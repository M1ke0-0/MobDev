-- DriveNext: схема для лабы №2.
-- Выполнить один раз в Supabase → SQL Editor → New query → Run.

-- 1. Профиль пользователя. id совпадает с id из auth.users (его создаёт Supabase Auth при signUp).
create table if not exists public.users (
    id                  uuid primary key references auth.users (id) on delete cascade,
    email               text not null,
    last_name           text not null,
    first_name          text not null,
    middle_name         text,
    birth_date          date not null,
    gender              text not null check (gender in ('male', 'female')),
    license_number      varchar(10) not null,
    license_issue_date  date not null,
    avatar_path         text,
    license_photo_path  text not null,
    passport_photo_path text not null,
    created_at          timestamptz not null default now()
);

-- Row Level Security: каждый видит и меняет только свою строку.
alter table public.users enable row level security;

create policy "users: read own" on public.users
    for select to authenticated using ((select auth.uid()) = id);
create policy "users: insert own" on public.users
    for insert to authenticated with check ((select auth.uid()) = id);
create policy "users: update own" on public.users
    for update to authenticated using ((select auth.uid()) = id) with check ((select auth.uid()) = id);

-- 2. Приватный бакет для фото: documents/<user_id>/avatar.jpg, license.jpg, passport.jpg
insert into storage.buckets (id, name, public)
values ('documents', 'documents', false)
on conflict (id) do nothing;

create policy "documents: read own" on storage.objects
    for select to authenticated
    using (bucket_id = 'documents' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy "documents: upload own" on storage.objects
    for insert to authenticated
    with check (bucket_id = 'documents' and (storage.foldername(name))[1] = (select auth.uid())::text);
create policy "documents: update own" on storage.objects
    for update to authenticated
    using (bucket_id = 'documents' and (storage.foldername(name))[1] = (select auth.uid())::text);
