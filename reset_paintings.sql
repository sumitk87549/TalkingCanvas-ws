-- Script to clear painting data and allow DataInitializer to reload with new images
-- Run this in PostgreSQL to reset painting data

-- Delete all painting-related data (order matters due to foreign keys)
DELETE FROM cart_items;
DELETE FROM painting_images;
DELETE FROM painting_certificates;
DELETE FROM painting_category_mapping;
DELETE FROM paintings;
DELETE FROM painting_categories;

-- Reset sequences if needed (optional)
-- This ensures IDs start from 1 again
ALTER SEQUENCE IF EXISTS paintings_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS painting_images_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS painting_certificates_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS painting_categories_id_seq RESTART WITH 1;
