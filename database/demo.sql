INSERT INTO categories (id, name, description) VALUES ('ab1a4b42-1957-495c-af89-903e51874900', 'Обучение', 'Демонстрационный шаблон');
INSERT INTO templates (id, category_id, slug) VALUES ('ab1a4b42-1957-495c-af89-903e51874901', 'ab1a4b42-1957-495c-af89-903e51874900', 'explain-concept');
INSERT INTO template_versions (id, template_id, version_number, title, body, status, published_at) VALUES ('ab1a4b42-1957-495c-af89-903e51874902', 'ab1a4b42-1957-495c-af89-903e51874901', 1, 'Объяснение темы', 'Объясни {{request}} для уровня {{level}}.', 'published', now());
INSERT INTO template_parameters (id, template_version_id, name, label, type, required, max_length, options, display_order) VALUES
('ab1a4b42-1957-495c-af89-903e51874903', 'ab1a4b42-1957-495c-af89-903e51874902', 'request', 'Тема', 'text', true, 10000, NULL, 0),
('ab1a4b42-1957-495c-af89-903e51874904', 'ab1a4b42-1957-495c-af89-903e51874902', 'level', 'Уровень', 'choice', true, 100, '["начальный", "средний", "продвинутый"]', 1);
