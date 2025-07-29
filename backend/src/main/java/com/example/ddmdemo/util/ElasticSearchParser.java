package com.example.ddmdemo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

public class ElasticSearchParser {

    enum TokenType { AND, OR, NOT, LPAREN, RPAREN, FIELD, PHRASE, WORD, EOF }

    static class Token {
        TokenType type;
        String text;
        Token(TokenType type, String text) {
            this.type = type;
            this.text = text;
        }
        public String toString() {
            return type + (text != null ? "(" + text + ")" : "");
        }
    }

    private List<Token> tokens;
    private int pos = 0;

    public ElasticSearchParser(String input) {
        this.tokens = tokenize(input);
    }

    private List<Token> tokenize(String input) {
        List<Token> list = new ArrayList<>();
        // Regex pattern matches operators, parentheses, quoted phrases, field names, words
        Pattern pattern = Pattern.compile(
            "\\s*(AND|OR|NOT|\\(|\\)|\"[^\"]*\"|\\w+:(?=\\S)|\\S+)\\s*",
            Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String token = matcher.group(1);
            switch (token.toUpperCase()) {
                case "AND": list.add(new Token(TokenType.AND, token)); break;
                case "OR": list.add(new Token(TokenType.OR, token)); break;
                case "NOT": list.add(new Token(TokenType.NOT, token)); break;
                case "(": list.add(new Token(TokenType.LPAREN, token)); break;
                case ")": list.add(new Token(TokenType.RPAREN, token)); break;
                default:
                    if (token.endsWith(":")) {
                        list.add(new Token(TokenType.FIELD, token.substring(0, token.length() - 1)));
                    } else if (token.startsWith("\"") && token.endsWith("\"")) {
                        list.add(new Token(TokenType.PHRASE, token.substring(1, token.length() - 1)));
                    } else {
                        list.add(new Token(TokenType.WORD, token));
                    }
            }
        }
        list.add(new Token(TokenType.EOF, null));
        return list;
    }

    private Token peek() { return tokens.get(pos); }
    private Token consume() { return tokens.get(pos++); }
    private boolean match(TokenType type) {
        if (peek().type == type) {
            consume();
            return true;
        }
        return false;
    }

    public Query parse() {
        Query q = parseOr();
        if (peek().type != TokenType.EOF) {
            throw new RuntimeException("Unexpected token: " + peek().text);
        }
        return q;
    }

    // OR operator: lowest precedence
    private Query parseOr() {
        Query left = parseAnd();
        while (match(TokenType.OR)) {
            Query right = parseAnd();

            Query finalLeft = left;
            Query finalRight = right;

            left = buildBool(bool -> bool
                .should(finalLeft)
                .should(finalRight)
                .minimumShouldMatch("1"));
        }
        return left;
    }

    // AND operator: middle precedence
    private Query parseAnd() {
        Query left = parseNot();
        while (match(TokenType.AND)) {
            Query right = parseNot();

            Query finalLeft = left;
            Query finalRight = right;

            left = buildBool(bool -> bool
                .must(finalLeft)
                .must(finalRight));
        }
        return left;
    }

    // NOT operator: highest precedence
    private Query parseNot() {
        if (match(TokenType.NOT)) {
            Query expr = parseNot();
            return buildBool(bool -> bool.mustNot(expr));
        }
        return parseTerm();
    }

    // Term: could be a field query, phrase, word, or parenthesis expression
    private Query parseTerm() {
        if (match(TokenType.LPAREN)) {
            Query expr = parseOr();
            if (!match(TokenType.RPAREN)) {
                throw new RuntimeException("Expected )");
            }
            return expr;
        }

        if (peek().type == TokenType.FIELD) {
            String field = consume().text;
            Token next = peek();
            if (next.type == TokenType.PHRASE) {
                consume();
                return buildMatchPhrase(field, next.text);
            } else if (next.type == TokenType.WORD) {
                consume();
                return buildMatch(field, next.text);
            } else {
                throw new RuntimeException("Expected phrase or word after field");
            }
        }

        if (peek().type == TokenType.PHRASE) {
            String phrase = consume().text;
            return buildMatchPhrase(null, phrase);
        }

        if (peek().type == TokenType.WORD) {
            String word = consume().text;
            return buildMatch(null, word);
        }

        throw new RuntimeException("Unexpected token: " + peek().text);
    }

    // Helper functions to create Elasticsearch Query DSL objects

    private Query buildMatch(String field, String text) {
        MatchQuery.Builder builder = new MatchQuery.Builder().query(text);
        if (field != null) builder.field(field);
        return new Query.Builder().match(builder.build()).build();
    }

    private Query buildMatchPhrase(String field, String phrase) {
        MatchPhraseQuery.Builder builder = new MatchPhraseQuery.Builder().query(phrase);
        if (field != null) builder.field(field);
        return new Query.Builder().matchPhrase(builder.build()).build();
    }

    private Query buildBool(java.util.function.Consumer<BoolQuery.Builder> consumer) {
        BoolQuery.Builder builder = new BoolQuery.Builder();
        consumer.accept(builder);
        return new Query.Builder().bool(builder.build()).build();
    }
}