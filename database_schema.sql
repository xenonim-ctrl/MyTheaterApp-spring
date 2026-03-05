-- =====================================================
-- SQL Schema for Theater Information System
-- PostgreSQL Database
-- =====================================================

-- Удаление существующих таблиц (если нужно пересоздать)
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS play_actors CASCADE;
DROP TABLE IF EXISTS performances CASCADE;
DROP TABLE IF EXISTS plays CASCADE;
DROP TABLE IF EXISTS halls CASCADE;
DROP TABLE IF EXISTS actors CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- =====================================================
-- Таблица пользователей
-- =====================================================
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    full_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    CONSTRAINT users_username_unique UNIQUE (username),
    CONSTRAINT users_email_unique UNIQUE (email)
);

-- Индексы для users
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_role ON users(role);

-- =====================================================
-- Таблица залов
-- =====================================================
CREATE TABLE halls (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    description VARCHAR(1000)
);

-- Индексы для halls
CREATE INDEX idx_halls_name ON halls(name);

-- =====================================================
-- Таблица актеров
-- =====================================================
CREATE TABLE actors (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(200) NOT NULL,
    role VARCHAR(200),
    bio VARCHAR(2000)
);

-- Индексы для actors
CREATE INDEX idx_actors_full_name ON actors(full_name);

-- =====================================================
-- Таблица спектаклей
-- =====================================================
CREATE TABLE plays (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(200) NOT NULL,
    director VARCHAR(200),
    description VARCHAR(2000),
    duration INTEGER NOT NULL CHECK (duration > 0),
    genre VARCHAR(100) NOT NULL
);

-- Индексы для plays
CREATE INDEX idx_plays_title ON plays(title);
CREATE INDEX idx_plays_author ON plays(author);
CREATE INDEX idx_plays_genre ON plays(genre);

-- =====================================================
-- Таблица показов (связь Play -> Performance)
-- =====================================================
CREATE TABLE performances (
    id BIGSERIAL PRIMARY KEY,
    play_id BIGINT NOT NULL,
    hall_id BIGINT NOT NULL,
    date_time TIMESTAMP NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    available_seats INTEGER NOT NULL CHECK (available_seats > 0),
    CONSTRAINT fk_performances_play FOREIGN KEY (play_id) 
        REFERENCES plays(id) ON DELETE CASCADE,
    CONSTRAINT fk_performances_hall FOREIGN KEY (hall_id) 
        REFERENCES halls(id) ON DELETE RESTRICT
);

-- Индексы для performances
CREATE INDEX idx_performances_play_id ON performances(play_id);
CREATE INDEX idx_performances_hall_id ON performances(hall_id);
CREATE INDEX idx_performances_date_time ON performances(date_time);
CREATE INDEX idx_performances_price ON performances(price);

-- =====================================================
-- Таблица билетов (связь Performance -> Ticket)
-- =====================================================
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    performance_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    seat_number INTEGER NOT NULL CHECK (seat_number > 0),
    purchase_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'PURCHASED' 
        CHECK (status IN ('PURCHASED', 'CANCELLED', 'USED')),
    CONSTRAINT fk_tickets_performance FOREIGN KEY (performance_id) 
        REFERENCES performances(id) ON DELETE CASCADE,
    CONSTRAINT fk_tickets_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_ticket_seat UNIQUE (performance_id, seat_number, status)
);

-- Индексы для tickets
CREATE INDEX idx_tickets_performance_id ON tickets(performance_id);
CREATE INDEX idx_tickets_user_id ON tickets(user_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_purchase_date ON tickets(purchase_date);

-- =====================================================
-- Таблица связи спектаклей и актеров (Many-to-Many)
-- =====================================================
CREATE TABLE play_actors (
    play_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    PRIMARY KEY (play_id, actor_id),
    CONSTRAINT fk_play_actors_play FOREIGN KEY (play_id) 
        REFERENCES plays(id) ON DELETE CASCADE,
    CONSTRAINT fk_play_actors_actor FOREIGN KEY (actor_id) 
        REFERENCES actors(id) ON DELETE CASCADE
);

-- Индексы для play_actors
CREATE INDEX idx_play_actors_play_id ON play_actors(play_id);
CREATE INDEX idx_play_actors_actor_id ON play_actors(actor_id);

-- =====================================================
-- Вставка данных по умолчанию
-- =====================================================

-- Создание администратора (пароль: meadmin123, захеширован BCrypt)
-- ВАЖНО: В реальном приложении пароль должен быть захеширован через BCrypt
-- Здесь показан пример, но в DataInitializer пароль хешируется автоматически
INSERT INTO users (username, password, role, full_name, phone, email) 
VALUES (
    'admino', 
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- meadmin123 (BCrypt)
    'ADMIN',
    'Фортуна',
    '+79999999999',
    'admin@theater.com'
) ON CONFLICT (username) DO NOTHING;

-- Примеры залов
INSERT INTO halls (name, capacity, description) VALUES
    ('Большой зал', 500, 'Главный зал театра с отличной акустикой'),
    ('Малый зал', 150, 'Камерный зал для небольших постановок'),
    ('Экспериментальный зал', 100, 'Зал для экспериментальных спектаклей')
ON CONFLICT DO NOTHING;

-- Примеры актеров
INSERT INTO actors (full_name, role, bio) VALUES
    ('Иван Иванов', 'Главная роль', 'Опытный актер с 20-летним стажем'),
    ('Мария Петрова', 'Второстепенная роль', 'Молодая талантливая актриса'),
    ('Александр Сидоров', 'Эпизодическая роль', 'Актер комедийного жанра')
ON CONFLICT DO NOTHING;

-- Примеры спектаклей
INSERT INTO plays (title, author, director, description, duration, genre) VALUES
    ('Гамлет', 'Уильям Шекспир', 'Иван Режиссеров', 'Классическая трагедия о принце Датском', 180, 'Трагедия'),
    ('Ревизор', 'Николай Гоголь', 'Петр Постановщиков', 'Комедия о чиновниках и взяточниках', 120, 'Комедия'),
    ('Чайка', 'Антон Чехов', 'Мария Режиссерова', 'Драма о любви и творчестве', 150, 'Драма')
ON CONFLICT DO NOTHING;

-- =====================================================
-- Комментарии к таблицам
-- =====================================================
COMMENT ON TABLE users IS 'Пользователи системы (администраторы и обычные пользователи)';
COMMENT ON TABLE halls IS 'Залы театра';
COMMENT ON TABLE actors IS 'Актеры театра';
COMMENT ON TABLE plays IS 'Спектакли (родительская сущность)';
COMMENT ON TABLE performances IS 'Показы спектаклей (дочерняя сущность для plays)';
COMMENT ON TABLE tickets IS 'Билеты на показы (дочерняя сущность для performances)';
COMMENT ON TABLE play_actors IS 'Связь многие-ко-многим между спектаклями и актерами';

-- =====================================================
-- Представления (Views) для удобства
-- =====================================================

-- Представление для просмотра информации о билетах
CREATE OR REPLACE VIEW tickets_info AS
SELECT 
    t.id AS ticket_id,
    t.seat_number,
    t.purchase_date,
    t.status,
    p.title AS play_title,
    perf.date_time AS performance_date,
    h.name AS hall_name,
    u.full_name AS user_name,
    u.email AS user_email,
    perf.price AS ticket_price
FROM tickets t
JOIN performances perf ON t.performance_id = perf.id
JOIN plays p ON perf.play_id = p.id
JOIN halls h ON perf.hall_id = h.id
JOIN users u ON t.user_id = u.id;

-- Представление для статистики по спектаклям
CREATE OR REPLACE VIEW plays_statistics AS
SELECT 
    p.id,
    p.title,
    p.genre,
    COUNT(DISTINCT perf.id) AS total_performances,
    COUNT(DISTINCT t.id) AS total_tickets_sold,
    COALESCE(SUM(CASE WHEN t.status = 'PURCHASED' THEN perf.price ELSE 0 END), 0) AS total_revenue
FROM plays p
LEFT JOIN performances perf ON p.id = perf.play_id
LEFT JOIN tickets t ON perf.id = t.performance_id AND t.status = 'PURCHASED'
GROUP BY p.id, p.title, p.genre;

-- =====================================================
-- Функции для проверки целостности данных
-- =====================================================

-- Функция проверки доступности места
CREATE OR REPLACE FUNCTION check_seat_availability(
    p_performance_id BIGINT,
    p_seat_number INTEGER
) RETURNS BOOLEAN AS $$
DECLARE
    v_available_seats INTEGER;
    v_sold_seats INTEGER;
BEGIN
    -- Получаем количество доступных мест
    SELECT available_seats INTO v_available_seats
    FROM performances
    WHERE id = p_performance_id;
    
    -- Проверяем, не занято ли место
    SELECT COUNT(*) INTO v_sold_seats
    FROM tickets
    WHERE performance_id = p_performance_id
      AND seat_number = p_seat_number
      AND status = 'PURCHASED';
    
    -- Место доступно, если номер места <= доступных мест и место не продано
    RETURN p_seat_number <= v_available_seats AND v_sold_seats = 0;
END;
$$ LANGUAGE plpgsql;

-- =====================================================
-- Триггеры для автоматического обновления
-- =====================================================

-- Триггер для проверки даты показа (не должна быть в прошлом при создании)
CREATE OR REPLACE FUNCTION check_performance_date()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.date_time < CURRENT_TIMESTAMP THEN
        RAISE EXCEPTION 'Дата показа не может быть в прошлом';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_performance_date
    BEFORE INSERT OR UPDATE ON performances
    FOR EACH ROW
    EXECUTE FUNCTION check_performance_date();

-- =====================================================
-- Права доступа (если используется отдельный пользователь БД)
-- =====================================================
-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO theater_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO theater_user;

-- =====================================================
-- Конец скрипта
-- =====================================================

