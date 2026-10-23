-- DriveNext: дополнение для лабы №4 (экран «Детали» и «Оформление аренды»).
-- Выполнить один раз в Supabase → SQL Editor → New query → Run (после schema_lab3.sql).
-- Избранное и бронирования хранятся локально на устройстве (Room), на сервере их нет.

-- Адрес, где стоит машина, и описание для экрана «Детали».
alter table public.cars add column if not exists address text not null default 'Авиамоторная ул., 8, стр. 2';
alter table public.cars add column if not exists description text;

update public.cars set description = 'Флагманский седан с 4,0-литровым V8 мощностью 435 л.с., пневмоподвеской и полным приводом 4MATIC. Просторный салон с массажными креслами.'
where model = 'S 500 Sedan';
update public.cars set description = 'Спортивный седан с 4,4-литровым V8 мощностью 625 л.с. Разгон до 100 км/ч за 3,3 секунды, полный привод xDrive.'
where model = 'M5';
update public.cars set description = 'Универсал бизнес-класса с 2,0-литровым дизельным двигателем, большим багажником и полным приводом quattro.'
where model = 'A6 Avant';
update public.cars set description = 'Электрический седан с запасом хода до 500 км, автопилотом и большим сенсорным экраном. Разгон до 100 км/ч за 6 секунд.'
where model = 'Model 3';
update public.cars set description = 'Комфортный гибридный седан: экономичный расход около 5 л на 100 км, тихий салон и вариатор.'
where model = 'Camry';
update public.cars set description = 'Компактный городской седан с механической коробкой передач. Экономичный и простой в управлении.'
where model = 'Rio';
update public.cars set description = 'Надёжный городской седан с механической коробкой и вместительным багажником на 460 литров.'
where model = 'Polo';
update public.cars set description = 'Легендарный внедорожник с 4,0-литровым V8 мощностью 585 л.с., тремя блокировками дифференциалов и роскошным салоном.'
where model = 'G 63 AMG';
update public.cars set description = 'Популярный городской седан с автоматической коробкой передач и низким расходом топлива.'
where model = 'Solaris';
update public.cars set description = 'Электрический спорткар с двумя моторами, запасом хода до 450 км и разгоном до 100 км/ч за 2,8 секунды.'
where model = 'Taycan';
