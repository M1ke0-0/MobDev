-- DriveNext: схема для лабы №3 (каталог автомобилей).
-- Выполнить один раз в Supabase → SQL Editor → New query → Run (после schema.sql из лабы 2).

-- 1. Автомобили для аренды.
create table if not exists public.cars (
    id            uuid primary key default gen_random_uuid(),
    brand         text not null,
    model         text not null,
    price_per_day integer not null check (price_per_day > 0),
    transmission  text not null check (transmission in ('automatic', 'manual')),
    fuel_type     text not null check (fuel_type in ('petrol', 'diesel', 'electric', 'hybrid')),
    -- Имя файла в публичном бакете cars (например, mercedes_s500.png) или полная https-ссылка
    image_path    text,
    is_available  boolean not null default true,
    created_at    timestamptz not null default now()
);

-- Каталог открыт на чтение всем; менять его из приложения нельзя (нет политик insert/update/delete).
alter table public.cars enable row level security;

create policy "cars: read all" on public.cars
    for select to anon, authenticated using (true);

-- 2. Публичный бакет для фото машин: ссылка вида .../storage/v1/object/public/cars/<файл>
insert into storage.buckets (id, name, public)
values ('cars', 'cars', true)
on conflict (id) do nothing;

-- 3. Тестовые данные. Фото можно загрузить в Storage → cars с такими же именами файлов;
--    пока фото нет, приложение показывает силуэт машины.
insert into public.cars (brand, model, price_per_day, transmission, fuel_type, image_path) values
    ('Mercedes-Benz', 'S 500 Sedan',   2500, 'automatic', 'petrol',   'mercedes_s500.png'),
    ('BMW',           'M5',            3200, 'automatic', 'petrol',   'bmw_m5.png'),
    ('Audi',          'A6 Avant',      2100, 'automatic', 'diesel',   'audi_a6.png'),
    ('Tesla',         'Model 3',       2800, 'automatic', 'electric', 'tesla_model3.png'),
    ('Toyota',        'Camry',         1800, 'automatic', 'hybrid',   'toyota_camry.png'),
    ('Kia',           'Rio',            900, 'manual',    'petrol',   'kia_rio.png'),
    ('Volkswagen',    'Polo',          1000, 'manual',    'petrol',   'vw_polo.png'),
    ('Mercedes-Benz', 'G 63 AMG',      6500, 'automatic', 'petrol',   'mercedes_g63.png'),
    ('Hyundai',       'Solaris',        950, 'automatic', 'petrol',   'hyundai_solaris.png'),
    ('Porsche',       'Taycan',        7000, 'automatic', 'electric', 'porsche_taycan.png');

-- Пример «машина сейчас занята» — её не будет ни на главной, ни в поиске.
update public.cars set is_available = false where model = 'Taycan';
