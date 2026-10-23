# DriveNext — разбор лабы 3 (учим по шагам)

> Как читать: сначала **рассказ своими словами** (раздел 1), потом **новые понятия с нуля** (2), потом **что происходит внутри при каждом действии пользователя** (3), потом **каждый новый файл** (4). В конце — речь на 1 минуту и вопросы для самопроверки с ответами под спойлером.
> Короткая версия и ответы на 10 контрольных вопросов из методички — в `Лаба3_шпаргалка.md`.

---

## 0. Одной фразой

В лабе 3 у приложения появился **настоящий главный экран**: снизу меню из трёх вкладок, на «Главной» — список машин с сервера и поиск, в «Настройках» — профиль пользователя со сменой аватара и выходом из аккаунта.

До лабы 3 `MainActivity` была просто заглушкой с текстом «появится в следующих лабах». Теперь это контейнер для всех основных экранов.

---

## 1. Что я сделал (рассказ)

1. **Нижнее меню.** `MainActivity` теперь состоит из двух частей: сверху контейнер для фрагментов, снизу `BottomNavigationView` с тремя иконками: дом, закладка, шестерёнка. Пункты меню описаны в XML-ресурсе `res/menu/bottom_nav.xml`. При нажатии на иконку в контейнер подставляется фрагмент этой вкладки.
2. **Главная.** Сверху сиреневая шапка с полем поиска (`SearchView`) и надписью «Давайте найдем автомобиль». Ниже — список карточек машин (`RecyclerView`). Список загружается из таблицы `cars` в Supabase при открытии экрана. Пока идёт загрузка, крутится лоадер. Если ошибка — текст «Не удалось загрузить данные. Попробуйте снова.» и кнопка «Повторить».
3. **Карточка машины.** Модель, марка, цена «2500₽ в день», коробка (А/Т) и топливо (Бензин), фото справа, кнопки «Забронировать» и «Детали». Фото грузится по ссылке библиотекой **Coil**.
4. **Поиск.** Ввёл «bmw» и нажал «Найти» на клавиатуре — открывается экран «Результаты поиска», там лоадер и запрос к серверу. На этом экране запрос можно поправить, и список обновится сам через 0,4 секунды после того, как перестал печатать.
5. **Настройки.** Аватар, имя, почта (из таблицы `users`), по нажатию — «Профиль». Дальше меню: «Мои бронирования», «Тема», «Уведомления», «Подключить свой автомобиль», «Помощь», «Пригласи друга».
6. **Профиль.** Большой аватар — нажал, выбрал фото в галерее, и оно загрузилось в Supabase Storage. Под аватаром имя и «Присоединился в июле 2024», ниже почта, «Поменять пароль», пол, Google-аккаунт и «Выйти из профиля».
7. **Выход.** «Выйти из профиля» завершает сессию в Supabase, стирает токен с телефона и открывает экран входа **с очисткой стека**: кнопка «Назад» уже не вернёт в приложение.
8. **Заглушки.** «Детали», «Оформление аренды», «Мои бронирования», «Избранное», «Подключить свой автомобиль» — это экраны лаб 4–5. Пока они открывают один общий экран-заглушку `ComingSoonFragment`.

---

## 2. Новые понятия — объяснение с нуля

### 2.1. Activity и Fragment: почему вкладки — фрагменты

- **Activity** — целое окно приложения (как `LoginActivity`).
- **Fragment** — кусок интерфейса со своей разметкой и жизненным циклом, который живёт **внутри** Activity.

Нижнее меню должно быть видно на всех вкладках. Если бы каждая вкладка была отдельной Activity, меню пришлось бы рисовать заново в каждой, и при переключении экран бы «моргал». Поэтому делаем **одну Activity с меню** и меняем только фрагмент в контейнере над ним.

```
┌──────────────────────────────┐
│                              │
│   FragmentContainerView      │  ← сюда подставляется HomeFragment / SettingsFragment / ...
│   (id = container)           │
│                              │
├──────────────────────────────┤
│  🏠        🔖        ⚙       │  ← BottomNavigationView (всегда на месте)
└──────────────────────────────┘
```

**Жизненный цикл фрагмента — что важно знать:**
- `onCreateView` — создать View из разметки (у нас через ViewBinding).
- `onViewCreated` — повесить слушатели и подписаться на ViewModel.
- `onStart` — экран стал виден (мы здесь вызываем `viewModel.onScreenStarted()`).
- `onDestroyView` — View уничтожена, но сам фрагмент может жить дальше (например, в back stack). Поэтому обнуляем `_binding = null`, иначе будет **утечка памяти**: фрагмент держал бы ссылки на мёртвые View.

### 2.2. FragmentManager и back stack

`supportFragmentManager` управляет фрагментами Activity. Изменения делаются **транзакцией**:

```kotlin
supportFragmentManager.commit {
    setReorderingAllowed(true)            // разрешить оптимизацию порядка операций
    replace(R.id.container, fragment)     // убрать то, что было в контейнере, поставить новый
    addToBackStack(null)                  // запомнить, чтобы «Назад» отменил эту транзакцию
}
```

- **Без `addToBackStack`** — фрагмент просто заменяется, «Назад» на него не вернёт. Так открываются **корни вкладок**.
- **С `addToBackStack`** — транзакция запоминается, «Назад» её отменяет. Так открываются **вложенные экраны**: «Результаты поиска», «Профиль», заглушки.
- `popBackStack(null, POP_BACK_STACK_INCLUSIVE)` — отменить **все** запомненные транзакции, то есть вернуться к корню вкладки.

Пример: Настройки → Профиль. Стек: `[Профиль]` поверх корня `Настройки`. Нажали «Назад» → снова Настройки. Нажали на вкладку «Главная» → стек очищается, ставится `HomeFragment`.

### 2.3. BottomNavigationView и ресурс меню

```xml
<!-- res/menu/bottom_nav.xml -->
<menu>
    <item android:id="@+id/nav_home"      android:icon="@drawable/ic_nav_home"      android:title="@string/nav_home" />
    <item android:id="@+id/nav_favorites" android:icon="@drawable/ic_nav_favorites" android:title="@string/nav_favorites" />
    <item android:id="@+id/nav_settings"  android:icon="@drawable/ic_nav_settings"  android:title="@string/nav_settings" />
</menu>
```

В разметке `activity_main.xml`:
- `app:menu="@menu/bottom_nav"` — откуда брать пункты;
- `app:labelVisibilityMode="unlabeled"` — без подписей (как в макете). `title` всё равно нужен: его зачитывает TalkBack для незрячих;
- `app:itemIconTint="@color/bottom_nav_item"` — цвет иконок. Это **селектор** (`res/color/bottom_nav_item.xml`): если `state_checked="true"`, иконка фиолетовая, иначе серая.

В коде:
```kotlin
binding.bottomNav.setOnItemSelectedListener { item -> showTab(item.itemId); true }  // true = «да, выбрать пункт»
binding.bottomNav.setOnItemReselectedListener { popToTabRoot() }                     // нажали на уже выбранную вкладку
```

### 2.4. RecyclerView — как он экономит память

Представь 1000 машин. Если создать 1000 карточек сразу, будет медленно и съест память. RecyclerView делает иначе:

```
  экран телефона                 RecyclerView держит ~5–7 карточек
 ┌─────────────┐
 │ карточка 1  │ ◄─┐
 │ карточка 2  │   │  прокрутили вниз: карточка 1 ушла за край,
 │ карточка 3  │   │  её View НЕ удаляется, а заполняется данными
 └─────────────┘   │  машины №4 и показывается снизу
   карточка 4  ────┘  (это и есть «recycle» — переиспользование)
```

Три участника:

| Кто | Что делает | У нас |
|---|---|---|
| **LayoutManager** | Где и как расставить карточки, прокрутка | `GridLayoutManager` с 1 колонкой на телефоне, 2–3 на планшете |
| **Adapter** | Сколько элементов, как создать карточку, как заполнить её данными | `CarAdapter` |
| **ViewHolder** | Держит ссылки на View одной карточки, чтобы не искать их при каждой прокрутке | `CarAdapter.CarViewHolder` (внутри — `ItemCarBinding`) |

Методы адаптера:
- `onCreateViewHolder` — **создать** новую карточку (inflate `item_car.xml`). Вызывается редко.
- `onBindViewHolder` — **заполнить** готовую карточку данными машины на позиции `position`. Вызывается при каждом появлении карточки на экране.
- `getItemCount` — сколько всего элементов.

### 2.5. ListAdapter и DiffUtil

Мы наследуемся не от голого `RecyclerView.Adapter`, а от `ListAdapter`:
- `getItemCount()` уже написан внутри: это размер текущего списка;
- новый список передаём через `adapter.submitList(cars)`;
- `DiffUtil` **в фоновом потоке** сравнивает старый и новый списки и обновляет только изменившиеся карточки (с анимацией).

Для сравнения мы пишем `CarDiff`:
```kotlin
areItemsTheSame(old, new)    = old.id == new.id   // это та же машина?
areContentsTheSame(old, new) = old == new         // у неё что-то поменялось? (data class сравнивает все поля)
```

Альтернатива — `notifyDataSetChanged()`. Она перерисовывает всё и без анимаций, поэтому хуже.

### 2.6. SearchView

Поле поиска с лупой. Главное — слушатель `OnQueryTextListener` с двумя методами:

| Метод | Когда вызывается | Что делаем у нас |
|---|---|---|
| `onQueryTextSubmit(query)` | Нажали «Найти»/Enter на клавиатуре | Главная: открыть «Результаты поиска». Результаты: спрятать клавиатуру |
| `onQueryTextChange(newText)` | Каждое изменение текста | Главная: ничего. Результаты: передать текст во ViewModel |

Возврат `true` означает «я обработал событие сам», `false` — «пусть SearchView сделает своё действие по умолчанию».

Атрибуты в разметке: `app:iconifiedByDefault="false"` (поле сразу раскрыто, а не одна иконка), `app:queryHint` (подсказка), `android:imeOptions="actionSearch"` (на клавиатуре кнопка «Найти»), `app:queryBackground="@android:color/transparent"` (убираем стандартное подчёркивание).

`searchView.clearFocus()` убирает фокус с поля, и клавиатура прячется.

### 2.7. Debounce — не слать запрос на каждую букву

Пользователь печатает «mercedes» — это 8 изменений текста. Если на каждое слать запрос, получится 8 запросов, и ответы могут прийти в неправильном порядке. Решение — **debounce**:

```kotlin
query.drop(1)              // первое значение (исходный запрос) пропускаем — его уже загрузили в init
    .debounce(400)         // пропустить значение дальше, только если 400 мс после него ничего не менялось
    .onEach { load() }     // тогда и грузим
    .launchIn(viewModelScope)
```

```
печатает:  m  e  r  c  e  d  e  s ......(пауза 400 мс)
запросов:  —  —  —  —  —  —  —  —   → 1 запрос «mercedes»
```

А если запрос всё-таки ушёл, а пользователь продолжил печатать, старый запрос **отменяется**: `loadJob?.cancel()` в `CarListViewModel.load()`. Так на экране не окажутся результаты по устаревшему тексту.

### 2.8. StateFlow, SavedStateHandle и «перезапуск приложения»

- `StateFlow` — «ящик» с текущим значением, на изменения которого можно подписаться. Одинаковое значение второй раз не отправляет.
- `SavedStateHandle` — хранилище внутри ViewModel, которое Android сохраняет в Bundle. Оно переживает **поворот** и даже **убийство процесса** системой: если свернуть приложение, Android выгрузит его из памяти, а потом пользователь вернётся.
- **Аргументы фрагмента автоматически попадают в `SavedStateHandle`.** Мы открываем результаты так: `SearchResultsFragment.newInstance("bmw")` → `arguments = bundleOf("query" to "bmw")`. ViewModel читает `savedStateHandle.getStateFlow("query", "")`.

Требование ТЗ «обновление результатов поиска при перезапуске приложения»: после восстановления создаётся **новая** ViewModel, её `init` вызывает `load()` с сохранённым запросом, и результаты загружаются с сервера заново.

### 2.9. Наследование ViewModel: CarListViewModel

«Главная» и «Результаты поиска» ведут себя одинаково (лоадер, ошибка, «Повторить», кнопки карточки) и отличаются только тем, **откуда берётся список**. Поэтому общая часть вынесена в абстрактный класс:

```kotlin
abstract class CarListViewModel(errorText) : ViewModel() {
    // состояние, события, load(), onRetryClick(), onBookClick(), onDetailsClick()...
    protected abstract val emptyText: Int                       // что писать, если пусто
    protected abstract suspend fun fetchCars(): Result<List<Car>>  // откуда брать машины
}

class HomeViewModel(...) : CarListViewModel(R.string.error_load_data) {
    override suspend fun fetchCars() = getCars()                 // все доступные
}

class SearchResultsViewModel(...) : CarListViewModel(R.string.error_search) {
    override suspend fun fetchCars() = searchCars(currentQuery)  // по запросу
}
```

Это шаблон **Template Method**: «скелет» алгоритма в базовом классе, а изменяемый шаг (`fetchCars`) — в наследниках. Плюс: тексты ошибок из ТЗ разные («загрузить данные» и «выполнить поиск») и передаются через конструктор.

### 2.10. Coil — картинки по ссылке

Android сам не умеет показывать картинку по URL в `ImageView`. Для этого нужна библиотека, у нас это **Coil**:

```kotlin
imageView.load(url) {
    crossfade(true)                         // плавное появление
    placeholder(R.drawable.img_car_placeholder)  // пока грузится
    error(R.drawable.img_car_placeholder)        // если не загрузилось
    fallback(R.drawable.img_car_placeholder)     // если url == null
}
```

Что Coil делает за нас: качает в фоновом потоке, уменьшает картинку под размер View, кэширует в памяти и на диске и **сам отменяет загрузку, если карточку переиспользовали** для другой машины. Без этого в быстрой прокрутке на карточке могла бы появиться чужая картинка.

### 2.11. Поиск на сервере: `ilike` и `or`

Запрос к PostgREST (это REST-API Supabase поверх PostgreSQL) собирается так:

```kotlin
postgrest["cars"].select(Columns.raw("id,brand,model,price_per_day,...")) {
    filter {
        eq("is_available", true)                 // только свободные
        words.forEach { word ->                  // для каждого слова запроса:
            or {
                ilike("brand", "%$word%")        //   есть в марке
                ilike("model", "%$word%")        //   ИЛИ есть в модели
            }
        }                                        // условия разных слов — через И
    }
    order("price_per_day", Order.ASCENDING)      // сначала дешёвые
}
```

- `ilike` — SQL `ILIKE`: поиск по шаблону **без учёта регистра**; `%` — «любые символы». `%bmw%` найдёт «BMW».
- Запрос «mercedes sedan» разбивается на слова: «mercedes» есть в марке, «sedan» — в модели, значит найдётся «Mercedes-Benz S 500 Sedan».
- Из слов вырезаются спецсимволы (`sanitizeWord`): запятая и скобки ломают синтаксис фильтра `or=(...)`, а `%` и `_` — это символы шаблона.
- `Columns.raw(...)` — явно перечисляем нужные колонки, чтобы лишние поля таблицы не ломали разбор JSON.

SQL-аналог того, что уходит на сервер:
```sql
select id, brand, model, ... from cars
where is_available = true
  and (brand ilike '%mercedes%' or model ilike '%mercedes%')
  and (brand ilike '%sedan%'    or model ilike '%sedan%')
order by price_per_day;
```

### 2.12. Публичный и приватный бакет, подписанная ссылка

В Supabase Storage файлы лежат в **бакетах** (папках верхнего уровня):

| Бакет | Доступ | Что там | Как получить ссылку |
|---|---|---|---|
| `cars` (лаба 3) | **публичный** | фото машин — их видят все | `storage.from("cars").publicUrl(path)` — постоянная ссылка |
| `documents` (лаба 2) | **приватный** + RLS | аватар, фото ВУ и паспорта | `createSignedUrl(path, 1.hours)` — **временная подписанная** ссылка |

**Подписанная ссылка** содержит токен и перестаёт работать через указанное время (у нас 1 час). Получить её может только владелец файла: RLS-политика «`documents: read own`» из `schema.sql` проверяет, что первая папка в пути равна `auth.uid()`.

### 2.13. RLS для каталога машин

В `schema_lab3.sql`:
```sql
alter table public.cars enable row level security;
create policy "cars: read all" on public.cars for select to anon, authenticated using (true);
```
- Читать каталог могут все (`using (true)`), даже без входа (`anon`).
- Политик на insert/update/delete **нет**, значит из приложения каталог изменить нельзя. Это защита: ключ `anon` лежит внутри APK, и его может достать кто угодно.

### 2.14. Общий профиль на два экрана (StateFlow в репозитории)

`ProfileRepositoryImpl` хранит последний загруженный профиль в `MutableStateFlow<UserProfile?>`. Подписаны на него и `SettingsViewModel`, и `ProfileViewModel`.

```
Профиль: сменили аватар → updateAvatar() → _profile.value = новый профиль
                                                │
                     ┌──────────────────────────┴───────────────────────┐
                     ▼                                                  ▼
        ProfileViewModel видит новый профиль           SettingsViewModel видит новый профиль
        (новое фото на экране «Профиль»)               (вернулись «Назад» — тут тоже новое фото)
```

Плюс: «Профиль» открывается мгновенно, потому что данные уже загрузили «Настройки», и второй запрос не нужен.

### 2.15. Сессия Supabase и SessionExpired

Supabase сам сохраняет сессию на диск и при запуске приложения **асинхронно** её читает. Поэтому перед `currentUserOrNull()` мы ждём `auth.awaitInitialization()`. Если сессии всё равно нет (её удалили на сервере или токен «битый»), бросаем новую ошибку `AppError.SessionExpired`. ViewModel в ответ делает выход и открывает экран входа.

### 2.16. Интерфейс MainNavigator

Фрагменты не должны знать, как устроена навигация в `MainActivity`. Поэтому есть интерфейс:

```kotlin
interface MainNavigator {
    fun openSearchResults(query: String)
    fun openProfile()
    fun openComingSoon(@StringRes titleRes: Int)
    fun openLogin()
}
```

`MainActivity` его реализует, а фрагмент вызывает `(requireActivity() as MainNavigator).openProfile()`. Если потом перейти на Navigation Component, поменяется только `MainActivity`, а фрагменты останутся как есть.

### 2.17. Адаптивная вёрстка через квалификаторы ресурсов

```
res/values/integers.xml         car_grid_columns = 1   ← телефон
res/values-w600dp/integers.xml  car_grid_columns = 2   ← ширина экрана ≥ 600dp (планшет, телефон боком)
res/values-w900dp/integers.xml  car_grid_columns = 3   ← ширина ≥ 900dp
```

Android сам выбирает подходящий файл. В коде одна строка: `GridLayoutManager(context, resources.getInteger(R.integer.car_grid_columns))`. Профиль на планшете не растягивается шире 480dp (`app:layout_constraintWidth_max="480dp"`).

---

## 3. Что происходит внутри — по шагам

### 3.1. Открыли «Главную»

```
MainActivity.onCreate (первый запуск) → showTab(nav_home) → HomeFragment в контейнер
HomeFragment создаёт HomeViewModel через Factory
HomeViewModel.init → load()
   state = isLoading=true                       → во фрагменте виден лоадер, список скрыт
   GetCarsUseCase → runRequest:
        интернета нет?  → Result.failure(NoConnection)
        есть           → CarRepositoryImpl.getAvailableCars()
                            → supabaseCall { postgrest["cars"].select{...}.decodeList<CarDto>() }
                            → CarDto.toDomain() → List<Car>
   успех  → state = cars=[...]                 → adapter.submitList(cars), лоадер скрыт
          → если список пустой — emptyText «Сейчас нет доступных автомобилей»
   ошибка → state = errorText «Не удалось загрузить данные…»  (+ кнопка «Повторить»)
          → NoConnection? событие OpenNoConnection (экран «Нет подключения»), retryWhenVisible = true
          → иначе событие ShowError → диалог с текстом ошибки сервера
```

**Автоповтор после «Нет подключения».** Экран `NoConnectionActivity` открывается поверх. Когда сеть появилась и пользователь нажал «Повторить попытку», он закрывается, `HomeFragment` снова виден → `onStart()` → `viewModel.onScreenStarted()` → раз `retryWhenVisible == true`, вызывается `load()`.

### 3.2. Нажали «Найти» на Главной

```
SearchView.onQueryTextSubmit("bmw")
   → clearFocus() (спрятать клавиатуру)
   → HomeViewModel.onSearchSubmit("bmw")  — пустой запрос игнорируется
   → событие CarListEvent.OpenSearchResults("bmw")
   → handleCarListEvent → navigator.openSearchResults("bmw")
   → MainActivity.push(SearchResultsFragment.newInstance("bmw"))  — c addToBackStack
SearchResultsViewModel.init → load() → SearchCarsUseCase("bmw") → CarRepositoryImpl.searchCars
```

### 3.3. Поменяли текст на экране результатов

```
onQueryTextChange("merc") → viewModel.onQueryChanged → savedStateHandle["query"] = "merc"
                                                     → StateFlow query выдаёт "merc"
                                                     → debounce ждёт 400 мс тишины
                                                     → load() (старый запрос отменён)
Стёрли всё → запрос пустой → fetchCars вернёт пустой список → «Введите марку или модель автомобиля»
```

### 3.4. «Забронировать» / «Детали»

```
кнопка в карточке → лямбда onBookClick(car), переданная в CarAdapter
   → viewModel.onBookClick(car) → событие OpenBooking(car)
   → navigator.openComingSoon(R.string.booking_title) → заглушка с заголовком «Оформление аренды»
```
Машина уже передаётся в событии, и в лабе 4 её можно будет отдать на экран бронирования.

### 3.5. Открыли «Настройки»

```
SettingsViewModel.init
   1) подписка на getProfile.profile (общий StateFlow) → state.profile
   2) refresh() → GetProfileUseCase.refresh() → ProfileRepositoryImpl.refreshProfile():
         awaitInitialization() → currentUserOrNull() (нет → SessionExpired)
         select email,first_name,last_name,gender,avatar_path,created_at from users where id = <мой id>
         avatar_path → createSignedUrl на 1 час
         собрать UserProfile → _profile.value = он
Фрагмент: пока профиля нет — лоадер вместо имени; потом имя, почта, аватар через Coil
```

Пункты меню:
| Пункт | Что происходит |
|---|---|
| Профиль | `OpenProfile` → `ProfileFragment` поверх |
| Мои бронирования | заглушка «Мои бронирования» |
| Подключить свой автомобиль | заглушка «Добавление автомобиля» |
| Тема / Уведомления / Помощь | Snackbar «Раздел появится в следующих версиях» |
| Пригласи друга | системное окно «Поделиться» (`Intent.ACTION_SEND` + `createChooser`) |

Все нажатия идут через `viewModel.onItemClick(SettingsItem.X)`, а ViewModel решает, какое событие отправить (MVVM).

### 3.6. Сменили аватар в Профиле

```
нажали на аватар → pickAvatar.launch(PickVisualMediaRequest(ImageOnly))   — системный выбор фото
выбрали фото → колбэк с Uri → viewModel.onAvatarPicked(uri)
   state.isSaving = true → затемнение + «Сохраняем…»
   UpdateAvatarUseCase → ProfileRepositoryImpl.updateAvatar(uri):
       PhotoUploader.upload(userId, "avatar", uri) → documents/<userId>/avatar.jpg (upsert — заменить старое)
       есть строка в users → update users set avatar_path = ... where id = ...
       нет строки (вошёл через Google, регистрацию не проходил) → путь кладём в метаданные пользователя Auth
       заново собрать профиль → _profile.value = новый  → обновятся оба экрана
   Snackbar «Фото профиля обновлено»
```

Это и есть требование ТЗ «Хранение данных: сохранение изменений профиля».

### 3.7. «Выйти из профиля»

```
viewModel.onSignOutClick() → SignOutUseCase:
    AuthRepositoryImpl.signOut():
        auth.signOut() на сервере; если сеть недоступна → auth.clearSession() (стереть локально)
        удалить access_token из SharedPreferences   ← по нему сплэш решал «пускать сразу на главную»
    ProfileRepository.clear()  → общий профиль = null (чтобы следующий пользователь не увидел чужие данные)
→ событие OpenLogin → openAsNewRoot(LoginActivity)  = FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
```

Выход **не требует интернета**: пользователь не должен застрять в аккаунте из-за плохой сети.

### 3.8. «Назад» в MainActivity

| Где находимся | Что делает «Назад» |
|---|---|
| Вложенный экран (Результаты, Профиль, заглушка) | `popBackStack` → корень вкладки |
| Корень «Настроек» или «Избранного» | Переключает на «Главную» (`OnBackPressedCallback backToHome`) |
| Корень «Главной» | Закрывает приложение |

`backToHome.isEnabled` пересчитывается при каждой смене вкладки и изменении back stack.

---

## 4. Все новые и изменённые файлы

Пометка **[новый]** или **[изменён]**.

### domain — бизнес-логика

| Файл | За что отвечает |
|---|---|
| `domain/model/Car.kt` [новый] | Машина: id, марка, модель, цена в день, `Transmission` (AUTOMATIC/MANUAL), `FuelType` (PETROL/DIESEL/ELECTRIC/HYBRID), ссылка на фото. |
| `domain/model/UserProfile.kt` [новый] | Профиль для экранов: имя, фамилия, почта, ссылка на аватар, пол, дата регистрации, Google-почта. Вычисляемое `fullName`. |
| `domain/model/AppError.kt` [изменён] | Добавлен `SessionExpired`. |
| `domain/repository/CarRepository.kt` [новый] | Интерфейс: `getAvailableCars()`, `searchCars(query)`. |
| `domain/repository/ProfileRepository.kt` [новый] | Интерфейс: общий `profile: StateFlow`, `refreshProfile()`, `updateAvatar(uri)`, `clear()`. |
| `domain/repository/AuthRepository.kt` [изменён] | Добавлен `signOut()`. |
| `domain/usecase/RunRequest.kt` [новый] | Функция `runRequest` перенесена из `SignInUseCase.kt` и стала обобщённой (`<T>`): теперь возвращает не только `Result<Unit>`, но и `Result<List<Car>>`, `Result<UserProfile>`. |
| `domain/usecase/GetCarsUseCase.kt` [новый] | Все доступные машины. |
| `domain/usecase/SearchCarsUseCase.kt` [новый] | Поиск (запрос обрезается от пробелов по краям). |
| `domain/usecase/GetProfileUseCase.kt` [новый] | `profile` (подписка) + `refresh()` (загрузить с сервера). |
| `domain/usecase/UpdateAvatarUseCase.kt` [новый] | Сменить аватар. |
| `domain/usecase/SignOutUseCase.kt` [новый] | Выход + очистка профиля. Без проверки интернета. |

### data — работа с Supabase

| Файл | За что отвечает |
|---|---|
| `data/remote/SupabaseProvider.kt` [изменён] | Константы `CARS_TABLE = "cars"`, `CARS_BUCKET = "cars"`. |
| `data/remote/PhotoUploader.kt` [новый] | Загрузка фото в `documents/<userId>/<имя>.jpg`. Раньше этот код был внутри `AuthRepositoryImpl`, теперь он общий для регистрации и смены аватара (не дублируем код). |
| `data/remote/dto/CarDto.kt` [новый] | `@Serializable` строка таблицы `cars` (snake_case → camelCase через `@SerialName`) + список колонок `COLUMNS`. |
| `data/remote/dto/ProfileRowDto.kt` [новый] | Только нужные колонки `users` для профиля, включая `created_at`. |
| `data/repository/CarRepositoryImpl.kt` [новый] | Запросы к `cars`: фильтр `is_available`, поиск через `or { ilike }`, сортировка по цене. Перевод `CarDto` → `Car`, путь к фото → публичная ссылка. |
| `data/repository/ProfileRepositoryImpl.kt` [новый] | Сбор профиля из таблицы `users` и данных Auth (метаданные, identities для Google), подписанная ссылка на аватар, смена аватара, кэш в `StateFlow`. |
| `data/repository/AuthRepositoryImpl.kt` [изменён] | Использует `PhotoUploader`; добавлен `signOut()`. |
| `di/AppContainer.kt` [изменён] | Создаёт `PhotoUploader`, `CarRepositoryImpl`, `ProfileRepositoryImpl` и новые use case. |

### presentation — экраны

| Файл | За что отвечает |
|---|---|
| `main/MainActivity.kt` [переписан] | Нижнее меню, переключение вкладок, `push()` вложенных экранов, «Назад» на «Главную». Реализует `MainNavigator`. |
| `main/MainNavigator.kt` [новый] | Интерфейс переходов для фрагментов. |
| `main/ComingSoonFragment.kt` [новый] | Заглушка с заголовком из аргументов; стрелка «Назад» по флагу `showBack`. |
| `cars/CarAdapter.kt` [новый] | `ListAdapter` + `ViewHolder` + `DiffUtil` для карточек машин. |
| `cars/CarListViewModel.kt` [новый] | Базовый класс ViewModel списка: `CarListUiState`, события `CarListEvent`, `load()` с отменой старого запроса, автоповтор после «Нет подключения». |
| `cars/CarListUi.kt` [новый] | `setupCarList` (сетка), `render` (лоадер/ошибка/пусто/список), `handleCarListEvent` (общая обработка событий). |
| `home/HomeFragment.kt`, `home/HomeViewModel.kt` [новые] | Главная. Поиск по Submit → «Результаты поиска». |
| `search/SearchResultsFragment.kt`, `search/SearchResultsViewModel.kt` [новые] | Результаты: запрос в `SavedStateHandle`, `debounce`. |
| `settings/SettingsFragment.kt`, `settings/SettingsViewModel.kt` [новые] | Настройки: шапка профиля, меню `SettingsItem`, «Поделиться». |
| `profile/ProfileFragment.kt`, `profile/ProfileViewModel.kt` [новые] | Профиль: выбор аватара, «Присоединился в …», смена пароля, выход. |
| `common/ImageLoading.kt` [новый] | `loadCarImage(url)` и `loadAvatar(url)` на Coil с заглушками. |
| `common/UiText.kt` [изменён] | Текст для `SessionExpired`. |

### Ресурсы

| Файл | За что отвечает |
|---|---|
| `menu/bottom_nav.xml` | Три пункта нижнего меню. |
| `color/bottom_nav_item.xml` | Селектор цвета иконок меню по `state_checked`. |
| `layout/activity_main.xml` | Контейнер фрагментов + `BottomNavigationView`. Старый `activity_placeholder.xml` удалён. |
| `layout/fragment_home.xml` | Сиреневая шапка с SearchView + RecyclerView + `include` состояний. |
| `layout/fragment_search_results.xml` | Тулбар «Результаты поиска», SearchView, список. |
| `layout/item_car.xml` | Карточка: `MaterialCardView` с рамкой, `Guideline` на 50% (текст слева, фото справа), две кнопки поровну. |
| `layout/view_list_state.xml` | Лоадер, ошибка с «Повторить», текст «пусто». Подключается через `<include>` в оба экрана списка. |
| `layout/fragment_settings.xml` | Шапка профиля + строки меню. Каждая строка — **один TextView** с иконкой слева (`drawableStartCompat`) и стрелкой справа (стиль `SettingsRow`). |
| `layout/fragment_profile.xml` | Аватар с фиолетовой обводкой и значком «+», пары «подпись — значение», разделители, затемнение с лоадером. |
| `layout/fragment_coming_soon.xml` | Заглушка. |
| `values/integers.xml`, `values-w600dp/`, `values-w900dp/` | Число колонок списка: 1 / 2 / 3. |
| `values/strings.xml` | Тексты лабы 3, тексты ошибок из ТЗ, `string-array months_prepositional` (январе…декабре). |
| `values/themes.xml` | Стили: маленькие кнопки карточки, строка настроек, подпись/значение профиля, разделитель. |
| `values/colors.xml` | `search_header` (сиреневый фон шапки), `card_stroke` (рамка карточки). |
| `drawable/ic_nav_*.xml` | Иконки меню: дом, закладка, шестерёнка. |
| `drawable/ic_bookings, ic_theme, ic_notifications, ic_add_car, ic_help, ic_invite, ic_chevron_right` | Иконки настроек. |
| `drawable/ic_transmission, ic_fuel` | Иконки характеристик в карточке (16dp). |
| `drawable/img_car_placeholder.xml` | Силуэт машины, пока нет фото. |
| `drawable/img_avatar_placeholder.xml` | Силуэт человека с отступами (`<inset>`), чтобы в круге выглядел как фото. |
| `drawable/bg_search_field.xml` | Белый скруглённый фон поля поиска. |

### Прочее

| Файл | За что отвечает |
|---|---|
| `app/build.gradle.kts` | Новые зависимости: `androidx.recyclerview:recyclerview:1.3.2`, `io.coil-kt:coil:2.7.0`. |
| `supabase/schema_lab3.sql` | Таблица `cars` (с `check` на допустимые значения коробки и топлива), RLS «читать могут все», публичный бакет `cars`, 10 тестовых машин (одна `is_available = false`). |

---

## 5. Типичные «почему так», которые могут спросить

- **Почему фрагменты, а не Activity на каждую вкладку?** Меню должно быть общим и не перерисовываться. Плюс фрагменты легче, а back stack внутри вкладки ведёт себя естественно.
- **Почему в `onDestroyView` пишем `_binding = null`?** Фрагмент в back stack живёт дольше своей View. Без обнуления он держит ссылки на уничтоженные View — утечка памяти.
- **Почему `viewLifecycleOwner.collectWhenStarted`, а не просто `collectWhenStarted`?** Подписка должна жить, пока жива **View** фрагмента, а не сам фрагмент. Иначе после возврата из back stack появятся две подписки и обращения к `binding == null`.
- **Почему поиск на сервере, а не фильтр готового списка?** В ТЗ: «результаты должны быть загружены с сервера на основе введённого запроса». И машин может быть тысячи — качать все ради поиска плохо.
- **Почему на экране и текст ошибки, и диалог?** ТЗ лабы: «показывать сообщение об ошибке и кнопку для повторной попытки». Общее требование курса: «текст ошибки сервера — в диалоговом окне».
- **Почему у машины цена `Int`, а не `Double`?** Цена в рублях за день целая; деньги в `Double` хранить нельзя из-за ошибок округления.
- **Почему перечисления (`Transmission`, `FuelType`), а не строки?** Domain не должен зависеть от того, как значения записаны в базе (`'automatic'`). Перевод строк в enum — в data-слое, а в подпись «А/Т» — в presentation (строковые ресурсы).
- **Почему «Профиль» не грузит данные заново?** Профиль уже лежит в общем `StateFlow` после «Настроек». Если его нет (например, процесс пересоздан), `ProfileViewModel` загрузит сам.
- **Почему «Присоединился/Присоединилась»?** В макете мужской род. Если в профиле пол женский, берётся строка `profile_joined_female`.
- **Зачем `awaitInitialization()`?** Сессия читается с диска асинхронно. Без ожидания сразу после запуска `currentUserOrNull()` мог бы вернуть `null`, хотя пользователь вошёл.

---

## 6. «Что сказать за 1 минуту» (лаба 3)

> «В третьей лабе я сделал главный экран с нижним меню. Это одна MainActivity с BottomNavigationView, пункты которого описаны в ресурсе res/menu. Вкладки — фрагменты: при выборе пункта я очищаю back stack и через FragmentManager подставляю фрагмент вкладки. Вложенные экраны, «Результаты поиска» и «Профиль», добавляю с addToBackStack, поэтому меню остаётся видно, а «Назад» возвращает к корню вкладки.
> На Главной список машин из таблицы cars в Supabase показывается в RecyclerView. Адаптер — ListAdapter с DiffUtil, раскладка — GridLayoutManager: одна колонка на телефоне и две-три на планшете. Фото грузит Coil. Есть лоадер, ошибка с кнопкой «Повторить», а без интернета — экран «Нет подключения» с автоповтором после возврата.
> Поиск — SearchView: на Главной по onQueryTextSubmit открываются результаты, а там по onQueryTextChange с debounce 400 мс уходит запрос с ilike по марке или модели. Запрос хранится в SavedStateHandle, поэтому после перезапуска результаты загружаются заново. Общая логика двух списков вынесена в базовый CarListViewModel.
> В Настройках и Профиле данные пользователя берутся из таблицы users. Аватар можно сменить через Photo Picker: он загружается в приватный бакет, а показывается по подписанной ссылке. Выход завершает сессию Supabase, стирает токен и открывает вход с очисткой стека».

---

## 7. Проверь себя

<details><summary>1. Что произойдёт, если убрать <code>addToBackStack(null)</code> при открытии «Профиля»?</summary>

Профиль заменит Настройки без записи в back stack. «Назад» не вернёт на Настройки: сработает следующее правило — переключение на Главную (или выход из приложения).
</details>

<details><summary>2. Какой метод адаптера вызывается чаще: onCreateViewHolder или onBindViewHolder? Почему?</summary>

`onBindViewHolder`. Карточки создаются один раз (сколько помещается на экран плюс запас), а потом при прокрутке только перезаполняются данными.
</details>

<details><summary>3. Почему в коде нет <code>getItemCount()</code>?</summary>

`CarAdapter` наследуется от `ListAdapter`, где `getItemCount()` уже реализован и возвращает размер списка, переданного в `submitList`.
</details>

<details><summary>4. Пользователь быстро напечатал «audi». Сколько запросов уйдёт на сервер с экрана результатов?</summary>

Один. `debounce(400)` пропустит значение, только когда после «audi» будет пауза 400 мс. Промежуточные «a», «au», «aud» отбросятся.
</details>

<details><summary>5. Где хранится текст запроса на экране результатов и почему именно там?</summary>

В `SavedStateHandle` у `SearchResultsViewModel` (туда же автоматически попадают аргументы фрагмента). Так он переживает поворот и убийство процесса, а после перезапуска результаты загрузятся заново.
</details>

<details><summary>6. Чем отличается <code>publicUrl</code> от <code>createSignedUrl</code>?</summary>

`publicUrl` — постоянная ссылка на файл в публичном бакете (`cars`), работает для всех. `createSignedUrl` — временная ссылка с токеном на файл в приватном бакете (`documents`), выдаётся только владельцу по RLS и истекает (у нас через 1 час).
</details>

<details><summary>7. Почему после смены аватара в Профиле он сразу новый и в Настройках?</summary>

Оба экрана подписаны на один `StateFlow profile` в `ProfileRepositoryImpl`. `updateAvatar` кладёт туда новый профиль, и обе ViewModel получают его.
</details>

<details><summary>8. Что найдёт запрос «bmw m5» и как это выглядит в фильтре?</summary>

BMW M5. Для каждого слова — `or(brand ilike %слово%, model ilike %слово%)`, а условия для разных слов объединяются через И: «bmw» нашлось в марке, «m5» — в модели.
</details>

<details><summary>9. Почему выход из аккаунта работает без интернета?</summary>

`SignOutUseCase` не вызывает `runRequest` (там проверка сети). `AuthRepositoryImpl.signOut()` при ошибке сервера делает `clearSession()` и всё равно удаляет токен из SharedPreferences.
</details>

<details><summary>10. Зачем нужен интерфейс <code>MainNavigator</code>?</summary>

Чтобы фрагменты не зависели от реализации навигации в `MainActivity`. Фрагмент вызывает `openProfile()` и не знает, как именно это сделано (FragmentManager сейчас, Navigation Component потом).
</details>

<details><summary>11. Как сделано, что на планшете 2–3 карточки в ряд?</summary>

Число колонок лежит в ресурсе `car_grid_columns` с разными значениями в `values/`, `values-w600dp/`, `values-w900dp/`. Android выбирает файл по ширине экрана, а `GridLayoutManager` берёт число колонок из ресурса.
</details>

<details><summary>12. Что увидит пользователь, если не выполнить <code>schema_lab3.sql</code>?</summary>

На Главной — «Не удалось загрузить данные. Попробуйте снова.» с кнопкой «Повторить» и диалог с ошибкой сервера (таблица `cars` не найдена).
</details>

<details><summary>13. Почему иконка «шестерёнка» видна, хотя подписи у пунктов меню отключены, и зачем тогда <code>title</code>?</summary>

`labelVisibilityMode="unlabeled"` скрывает только текст, иконки остаются. `title` нужен для доступности: его зачитывает TalkBack, и он показывается при долгом нажатии.
</details>

<details><summary>14. Зачем <code>loadJob?.cancel()</code> в <code>CarListViewModel.load()</code>?</summary>

Если пользователь поменял запрос, пока шёл старый, ответ на старый запрос уже не нужен и может прийти позже нового, перезаписав правильные результаты. Отмена гарантирует, что на экране будет ответ на последний запрос.
</details>
