-- Create posts table for Motivise study blogging platform
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(500) NOT NULL,
    title VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME
);

-- Insert some sample data for testing
INSERT INTO posts (content, title, created_at) VALUES 
('Just learned Spring Boot REST controllers! Amazing how @RestController works.', 'Spring Boot Study', NOW()),
('Working on my semester project. JPA makes database operations so much easier!', 'Project Progress', NOW()),
('Understanding HTTP status codes and ResponseEntity better now.', 'Web Development', NOW()),
('Flyway migrations are really useful for database versioning!', 'Database Learning', NOW());