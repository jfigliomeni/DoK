DROP TABLE for_sale_query_entity;

ALTER TABLE user_deck
    DROP COLUMN asking_price;
ALTER TABLE user_deck
    DROP COLUMN listing_info;
ALTER TABLE user_deck
    DROP COLUMN condition;
ALTER TABLE user_deck
    DROP COLUMN redeemed;
ALTER TABLE user_deck
    DROP COLUMN external_link;
ALTER TABLE user_deck
    DROP COLUMN date_listed;
ALTER TABLE user_deck
    DROP COLUMN expires_at;
ALTER TABLE user_deck
    DROP COLUMN currency_symbol;
ALTER TABLE user_deck
    DROP COLUMN language;
ALTER TABLE user_deck
    DROP COLUMN for_sale_in_country;
ALTER TABLE user_deck
    DROP COLUMN for_sale;
ALTER TABLE user_deck
    DROP COLUMN for_trade;
