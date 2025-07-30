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

    private static final String DEFAULT_FIELD = "content_sr";

    private List<Token> tokens;
    private int pos = 0;

    public ElasticSearchParser(String input) {
        this.tokens = tokenize(input);
    }

    private List<Token> tokenize(String input) {
        List<Token> list = new ArrayList<>();
        Pattern pattern = Pattern.compile(
        	    "\\s*(\\w+:|\"[^\"]*\"|AND|OR|NOT|\\(|\\)|\\w+|\\S)\\s*",
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
        for(int i = 0; i < list.size(); i++) {
        	System.err.println(list.get(i));
        }
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

    private Query parseAnd() {
        Query left = parseNot();

        while (true) {
            if (match(TokenType.AND)) {
                // Explicit AND
                Query right = parseNot();
                Query finalLeft = left;
                Query finalRight = right;
                left = buildBool(bool -> bool
                    .must(finalLeft)
                    .must(finalRight));
            } else if (isImplicitAnd(peek().type)) {
                // Implicit AND
                Query right = parseNot();
                Query finalLeft = left;
                Query finalRight = right;
                left = buildBool(bool -> bool
                    .must(finalLeft)
                    .must(finalRight));
            } else {
                break;
            }
        }
        return left;
    }

    private Query parseNot() {
        if (match(TokenType.NOT)) {
            Query expr = parseNot();
            return buildBool(bool -> bool.mustNot(expr));
        }
        return parseTerm();
    }

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
            return buildMatchPhrase(DEFAULT_FIELD, phrase);  // Use default field here
        }

        if (peek().type == TokenType.WORD) {
            String word = consume().text;
            return buildMatch(DEFAULT_FIELD, word);          // Use default field here
        }

        throw new RuntimeException("Unexpected token: " + peek().text);
    }

    private Query buildMatch(String field, String text) {
        MatchQuery.Builder builder = new MatchQuery.Builder().query(text);
        builder.field(field);  // Field is never null now
        return new Query.Builder().match(builder.build()).build();
    }

    private Query buildMatchPhrase(String field, String phrase) {
        MatchPhraseQuery.Builder builder = new MatchPhraseQuery.Builder().query(phrase);
        builder.field(field);  // Field is never null now
        return new Query.Builder().matchPhrase(builder.build()).build();
    }

    private Query buildBool(java.util.function.Consumer<BoolQuery.Builder> consumer) {
        BoolQuery.Builder builder = new BoolQuery.Builder();
        consumer.accept(builder);
        return new Query.Builder().bool(builder.build()).build();
    }
    
    private boolean isImplicitAnd(TokenType type) {
        return type == TokenType.WORD ||
               type == TokenType.PHRASE ||
               type == TokenType.FIELD ||
               type == TokenType.LPAREN ||
               type == TokenType.NOT;
    }
}
