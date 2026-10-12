# DriveNext — рассказ про лабы 1 и 2

> Как пользоваться: сверху — короткий рассказ «своими словами» по каждой лабе, потом архитектура, потом файлы по порядку с пояснением, за что отвечает каждый.
> Подробные ответы на контрольные вопросы — в `Лаба1_шпаргалка.md` и `Лаба2_шпаргалка.md`.

---

## 0. Одной фразой

**DriveNext** — Android-приложение для аренды автомобилей на Kotlin. Все лабы — это одно приложение, которое дописывается шаг за шагом.
- **Лаба 1** — запуск приложения: сплэш, проверка интернета, экран «Нет подключения», онбординг из трёх слайдов.
- **Лаба 2** — вход и регистрация: выбор «Войти / Зарегистрироваться», экран входа, регистрация в 3 шага, экран «Поздравляем». Бэкенд — **Supabase**.

Общие требования курса: minSdk 31 (Android 12), **чистая архитектура** (data / domain / presentation), **MVVM** (логика во ViewModel), Git.

---

## 1. Лаба 1 — что я сделал (рассказ)

1. **Сплэш-экран.** При запуске показывается логотип через системный **SplashScreen API** (`installSplashScreen()` вызывается в `onCreate` **до** `super.onCreate()`). Системный сплэш умеет только иконку по центру, а в макете ещё название и слоган — поэтому после него 2 секунды показывается своя разметка `activity_splash.xml`.
2. **Проверка интернета.** Пока идёт сплэш, `SplashViewModel` через `NetworkRepository` спрашивает у `ConnectivityManager`, есть ли сеть с возможностью `NET_CAPABILITY_INTERNET`. Для этого в манифесте разрешения `INTERNET` и `ACCESS_NETWORK_STATE`.
3. **Экран «Нет подключения».** Если сети нет — открывается он. Кнопка «Повторить попытку» показывает лоадер и проверяет сеть снова; если сети всё ещё нет — Snackbar «Подключение по-прежнему отсутствует».
4. **Онбординг.** Три слайда в **ViewPager2**, каждый слайд — `Fragment`, адаптер — `FragmentStateAdapter`. Есть точки-индикатор, «Пропустить», «Далее», на последнем слайде — «Поехали».
5. **Запоминаем онбординг.** После «Пропустить»/«Поехали» в **SharedPreferences** пишется флаг `onboarding_completed`, и при следующих запусках онбординг не показывается.
6. **Куда идти после сплэша** решает одно правило — `GetStartDestinationUseCase`:
   - онбординг не пройден → онбординг;
   - нет access token → экран входа/регистрации;
   - иначе → главный экран (пока заглушка).

```
         запуск
           │
   [ SplashActivity ] ← 2 сек
           │
  есть интернет? ── нет ──► [ NoConnectionActivity ] ──«Повторить», сеть есть──┐
           │ да                                                                │
           ▼  ◄────────────────────────────────────────────────────────────────┘
  онбординг пройден? ── нет ──► [ OnboardingActivity ] ──► [ AuthChoiceActivity ]
           │ да
  есть токен? ── нет ──► [ AuthChoiceActivity ]
           │ да
           ▼
     [ MainActivity ] (заглушка)
```

**Технологии лабы 1:** SplashScreen API, ConnectivityManager, ViewPager2 + Fragment + FragmentStateAdapter, SharedPreferences, ViewModel + Flow, ViewBinding.

---

## 2. Лаба 2 — что я сделал (рассказ)

1. **Экран «Вход/регистрация»** (`AuthChoiceActivity`) — логотип, слоган и две кнопки. Логики нет, только переходы, поэтому ViewModel ему не нужна.
2. **Экран входа** (`LoginActivity`):
   - email + пароль с «глазиком»;
   - кнопка «Войти» активна, только когда оба поля заполнены (вычисляемое свойство `isSignInEnabled` в состоянии экрана);
   - email проверяется (должен заканчиваться на `.com` или `.ru`), ошибка — под полем;
   - ошибки сервера («Неверная почта или пароль») — в **диалоге**;
   - «Войти через Google» (OAuth через браузер с возвратом по диплинку `drivenext://login-callback`);
   - «Забыли пароль?» — письмо со сбросом пароля.
3. **Регистрация в 3 шага** — одна `RegistrationActivity` и три фрагмента с **общей ViewModel** (`activityViewModels()`):
   - **Шаг 1:** email, пароль, повтор пароля, галочка согласия;
   - **Шаг 2:** фамилия, имя, отчество (необязательно), дата рождения (**MaterialDatePicker** или ручной ввод с маской `DD/MM/YYYY`), пол;
   - **Шаг 3:** фото профиля (необязательно), номер ВУ (10 цифр), дата выдачи, фото ВУ и паспорта — из галереи (**PickVisualMedia**) или с камеры (**TakePicture** + **FileProvider**).
   Благодаря общей ViewModel «Назад» возвращает на предыдущий шаг с заполненными полями, данные не теряются при повороте.
4. **Отправка регистрации в Supabase:** создаётся пользователь в Auth → фото загружаются в Storage (бакет `documents/<userId>/...`) → профиль пишется в таблицу `users` (Postgrest).
5. **Экран «Поздравляем»** → «Далее» → главный экран **с очисткой стека** (кнопка «Назад» не вернёт на формы).
6. **Нет интернета во время запроса** → экран «Нет подключения» открывается **поверх** формы; когда сеть появилась — он закрывается, и пользователь возвращается к той же форме с заполненными полями.
7. **Токен:** после входа access token сохраняется в SharedPreferences, и при следующем запуске сплэш сразу пускает на главный экран.

```
 Splash ─► Onboarding ─► [ AuthChoice ] ─«Войти»─► [ Login ] ─успех─► Main (стек очищен)
                              │                        │ «Зарегистрироваться»
                              └─«Зарегистрироваться»─► [ Registration: шаг 1 → 2 → 3 ]
                                                                  │ успех
                                                                  ▼
                                               [ RegistrationSuccess ] ─«Далее»─► Main
```

**Технологии лабы 2:** MaterialDatePicker, Activity Result API (PickVisualMedia, TakePicture), FileProvider, Supabase (Auth, Postgrest, Storage), Kotlin Coroutines, StateFlow, SavedStateHandle, kotlinx.serialization.

---

## 3. Архитектура проекта

### Три слоя (чистая архитектура)

```
 ┌──────────────────────────────┐
 │        presentation          │  Activity / Fragment (показывают)  +  ViewModel (думают)
 └──────────────┬───────────────┘
                │ вызывает use case
                ▼
 ┌──────────────────────────────┐
 │           domain             │  модели, интерфейсы репозиториев, use case, валидация
 │   (чистый Kotlin, без Android)│
 └──────────────▲───────────────┘
                │ реализует интерфейсы
 ┌──────────────┴───────────────┐
 │            data              │  SharedPreferences, ConnectivityManager, Supabase
 └──────────────────────────────┘
```

**Главная мысль для защиты:** зависимости направлены **внутрь, к domain**. Domain ничего не знает ни про Android, ни про Supabase — в нём только правила и интерфейсы (`AuthRepository`, `NetworkRepository`). Data-слой эти интерфейсы реализует. ViewModel работает только через use case и интерфейсы, поэтому, например, Supabase можно заменить на другой бэкенд, не трогая экраны.

### MVVM — как работает один экран

```
 Пользователь жмёт кнопку
        │
        ▼
 Activity/Fragment ──► viewModel.onSignInClick()
                              │
                              ▼
                       UseCase (проверил интернет → вызвал репозиторий)
                              │
                              ▼
                       Result.success / Result.failure(AppError)
                              │
                              ▼
 Activity.render(state) ◄── StateFlow<UiState>   (состояние экрана)
 Activity показывает   ◄── Channel/Flow<Event>   (разовые события: переход, диалог)
```

- **State** (`StateFlow`) — то, что сейчас на экране: тексты полей, ошибки, лоадер, активность кнопок. Переживает поворот.
- **Event** (`Channel`) — одноразовое действие: «перейти на экран», «показать диалог», «нет сети».
- Activity только **рисует** состояние и передаёт нажатия во ViewModel.

### Как обрабатываются ошибки

1. Supabase кидает своё исключение →
2. `ErrorMapper` (`supabaseCall { }`) переводит его в наш `AppError` (например, `InvalidCredentials`, `UserAlreadyExists`) →
3. use case (`runRequest`) возвращает `Result.failure(AppError)`; до запроса он ещё проверяет интернет и при его отсутствии сразу отдаёт `AppError.NoConnection` →
4. ViewModel переводит ошибку в текст (`UiText`) →
5. Activity показывает диалог; если `NoConnection` — открывает экран «Нет подключения».

### Внедрение зависимостей (DI)

Без библиотек (Hilt/Koin) — **ручной DI**. `DriveNextApp` (класс Application) при старте создаёт `AppContainer`, а тот — все репозитории и use case в одном экземпляре. ViewModel получает их через `Factory` (`viewModelFactory { initializer { ... } }`).

---

## 4. Структура папок

```
MobDev/
├── Лаба1_шпаргалка.md, Лаба2_шпаргалка.md   ← подробные шпаргалки + контрольные вопросы
└── DriveNext/                                ← сам Android-проект
    ├── build.gradle.kts, settings.gradle.kts, gradle.properties   ← настройки сборки
    ├── local.properties                      ← ключи Supabase (НЕ в Git)
    ├── supabase/schema.sql                   ← таблица users, бакет documents, RLS
    └── app/
        ├── build.gradle.kts                  ← зависимости модуля
        └── src/
            ├── debug/                        ← только для отладочной сборки (ярлык «сброс»)
            └── main/
                ├── AndroidManifest.xml
                ├── java/com/mtuci/drivenext/
                │   ├── DriveNextApp.kt
                │   ├── di/
                │   ├── domain/   (model, repository, usecase, validation)
                │   ├── data/     (remote, repository)
                │   └── presentation/ (common, splash, noconnection, onboarding, auth, main)
                └── res/          (layout, drawable, values, color, xml)
```

---

## 5. За что отвечает каждый файл

Пометка **[Л1]** / **[Л2]** — в какой лабе файл появился.

### Корень приложения

| Файл | За что отвечает |
|---|---|
| `DriveNextApp.kt` [Л1] | Класс `Application` — создаётся первым при запуске. Создаёт `AppContainer`. Указан в манифесте `android:name`. |
| `di/AppContainer.kt` [Л1, дополнен в Л2] | Ручной DI: создаёт SharedPreferences, клиент Supabase, все репозитории и use case. Метод `resetAppData()` — очистка данных (для debug-ярлыка). |

### domain — бизнес-логика (чистый Kotlin)

| Файл | За что отвечает |
|---|---|
| `domain/model/StartDestination.kt` [Л1] | Перечисление, куда идти после сплэша: `ONBOARDING`, `AUTH`, `MAIN`. |
| `domain/model/AppError.kt` [Л2] | Наши типы ошибок (`NoConnection`, `InvalidCredentials`, `UserAlreadyExists`, `WeakPassword`, `Server`…) — чтобы верхние слои не зависели от исключений Supabase. |
| `domain/model/Gender.kt` [Л2] | Пол: мужской / женский. |
| `domain/model/RegistrationData.kt` [Л2] | Все данные с трёх шагов регистрации в одном объекте. Фото — строковые Uri, чтобы domain не зависел от Android. |
| `domain/repository/NetworkRepository.kt` [Л1] | Интерфейс: «есть ли интернет?». |
| `domain/repository/OnboardingRepository.kt` [Л1] | Интерфейс: «пройден ли онбординг?» / «отметить пройденным». |
| `domain/repository/AuthRepository.kt` [Л2] | Интерфейс авторизации: проверка токена, вход, Google, обработка диплинка, сброс пароля, регистрация. |
| `domain/usecase/GetStartDestinationUseCase.kt` [Л1] | Единственное правило «куда идти после сплэша». |
| `domain/usecase/SignInUseCase.kt` [Л2] | Вход по email/паролю. Здесь же общая функция `runRequest`: проверить интернет → выполнить запрос → вернуть `Result`. |
| `domain/usecase/RegisterUseCase.kt` [Л2] | Регистрация (через `runRequest`). |
| `domain/usecase/GoogleSignInUseCase.kt` [Л2] | Запуск входа через Google и завершение по диплинку. |
| `domain/usecase/ResetPasswordUseCase.kt` [Л2] | «Забыли пароль?» — отправка письма. |
| `domain/validation/Validators.kt` [Л2] | Проверки: email (`.com`/`.ru`), разбор даты `DD/MM/YYYY`, дата рождения (не в будущем, 18+), дата выдачи ВУ (после рождения, не в будущем), номер ВУ (10 цифр). |

### data — реализация (Android, Supabase)

| Файл | За что отвечает |
|---|---|
| `data/repository/NetworkRepositoryImpl.kt` [Л1] | Проверка сети через `ConnectivityManager` + `NetworkCapabilities.NET_CAPABILITY_INTERNET`. |
| `data/repository/OnboardingRepositoryImpl.kt` [Л1] | Флаг `onboarding_completed` в SharedPreferences. |
| `data/repository/AuthRepositoryImpl.kt` [Л2] | **Единственное место, которое знает про Supabase.** Вход, регистрация (Auth → Storage → таблица `users`), Google OAuth, сброс пароля, сохранение access token в SharedPreferences. |
| `data/remote/SupabaseProvider.kt` [Л2] | Создаёт клиент Supabase с модулями Auth (схема `drivenext://login-callback`, PKCE), Postgrest, Storage. URL и ключ берутся из `BuildConfig` (а туда — из `local.properties`). |
| `data/remote/ErrorMapper.kt` [Л2] | Обёртка `supabaseCall { }`: ловит исключения Supabase и превращает их в `AppError`. |
| `data/remote/dto/UserProfileDto.kt` [Л2] | `@Serializable` класс — одна строка таблицы `users` (как она лежит в БД). |

### presentation — экраны и ViewModel

**Общее**

| Файл | За что отвечает |
|---|---|
| `presentation/common/Navigation.kt` [Л1, дополнен в Л2] | Функции переходов: `navigateTo(destination)`, `openScreenAndFinish`, `openAsNewRoot` (с очисткой стека), `openNoConnection` (поверх текущего экрана), `appContainer()` для фабрик ViewModel. |
| `presentation/common/UiText.kt` [Л2] | Текст по id строкового ресурса + перевод `AppError` → понятное сообщение для пользователя. |
| `presentation/common/UiExtensions.kt` [Л2] | Помощники: `collectWhenStarted` (подписка на Flow, пока экран виден), `showErrorDialog`, `showError` под полем, `setTextIfChanged`. |
| `presentation/common/DateInput.kt` [Л2] | `DateMaskWatcher` — сам ставит слэши при вводе даты; `showDatePicker` — MaterialDatePicker (дата через UTC); `reattachDatePicker` — восстановление слушателя после поворота. |

**Лаба 1**

| Файл | За что отвечает |
|---|---|
| `splash/SplashActivity.kt` | Стартовый экран: `installSplashScreen()`, своя разметка, подписка на события ViewModel и переход. В debug-сборке обрабатывает ярлык «сброс». |
| `splash/SplashViewModel.kt` | Ждёт 2 сек → проверяет интернет → отдаёт событие `NoConnection` или `Navigate(куда)`. |
| `noconnection/NoConnectionActivity.kt` | Экран «Нет подключения»: кнопка «Повторить попытку», лоадер, Snackbar. Умеет работать в двух режимах: со сплэша (идти дальше) и поверх формы (просто закрыться и вернуть назад). |
| `noconnection/NoConnectionViewModel.kt` | Логика повторной проверки сети. |
| `onboarding/OnboardingActivity.kt` | ViewPager2, точки, кнопки «Пропустить» / «Далее» / «Поехали». |
| `onboarding/OnboardingViewModel.kt` | Текущий слайд, текст кнопки, сохранение флага «онбординг пройден». |
| `onboarding/OnboardingPagerAdapter.kt` | `FragmentStateAdapter`: сколько страниц и какой фрагмент на какой позиции. |
| `onboarding/OnboardingPageFragment.kt` | Один слайд (картинка, заголовок, текст). `_binding = null` в `onDestroyView` — против утечки памяти. |
| `onboarding/OnboardingPage.kt` | Модель слайда + список трёх слайдов `onboardingPages`. |
| `main/MainActivity.kt` | Главный экран — пока заглушка (будет в следующих лабах). |

**Лаба 2**

| Файл | За что отвечает |
|---|---|
| `auth/AuthChoiceActivity.kt` | Экран «Вход/регистрация»: две кнопки, только переходы. |
| `auth/login/LoginActivity.kt` | Экран входа: рисует состояние, показывает диалоги, принимает диплинк Google (`singleTask` + `onNewIntent`). |
| `auth/login/LoginViewModel.kt` | Состояние формы входа, валидация email, вызов `SignIn` / `GoogleSignIn` / `ResetPassword`, обработка ошибок. |
| `auth/register/RegistrationActivity.kt` | Контейнер для трёх шагов (фрагменты), стрелка «Назад», смена шагов. |
| `auth/register/RegistrationViewModel.kt` | **Общая** ViewModel трёх шагов: хранит все поля, валидирует каждый шаг, собирает `RegistrationData`, вызывает `RegisterUseCase`. Uri снимка камеры хранит в `SavedStateHandle`. |
| `auth/register/RegisterStep1Fragment.kt` | Шаг 1: email, пароль, повтор, согласие. |
| `auth/register/RegisterStep2Fragment.kt` | Шаг 2: ФИО, дата рождения (DatePicker + маска), пол. |
| `auth/register/RegisterStep3Fragment.kt` | Шаг 3: аватар, номер и дата выдачи ВУ, фото ВУ и паспорта — галерея (`PickVisualMedia`) или камера (`TakePicture` + `FileProvider`). |
| `auth/register/RegistrationSuccessActivity.kt` | «Поздравляем» → «Далее» → главный экран с очисткой стека. |

### Ресурсы (`res/`)

| Папка / файл | За что отвечает |
|---|---|
| `layout/activity_splash.xml` [Л1] | Разметка сплэша: название, слоган, логотип. |
| `layout/activity_no_connection.xml` [Л1] | Иконка, текст, кнопка «Повторить», лоадер. |
| `layout/activity_onboarding.xml` [Л1] | ViewPager2, точки, кнопки. |
| `layout/fragment_onboarding_page.xml` [Л1] | Разметка одного слайда. |
| `layout/activity_placeholder.xml` [Л1] | Общая разметка для экранов-заглушек (главный). |
| `layout/activity_auth_choice.xml` [Л2] | Экран выбора «Войти / Зарегистрироваться». |
| `layout/activity_login.xml` [Л2] | Форма входа. |
| `layout/activity_registration.xml` [Л2] | Шапка регистрации + контейнер для фрагментов шагов. |
| `layout/fragment_register_step1/2/3.xml` [Л2] | Формы трёх шагов. |
| `layout/activity_registration_success.xml` [Л2] | «Поздравляем». |
| `values/strings.xml` | Все тексты приложения (в т.ч. тексты ошибок). |
| `values/colors.xml` | Цвета из макета. |
| `values/themes.xml` | Основная тема `Theme.DriveNext` и тема сплэша `Theme.DriveNext.Splash` (`windowSplashScreenAnimatedIcon`, `postSplashScreenTheme`). |
| `color/text_field_stroke.xml` [Л2] | Цвет рамки поля ввода: основной цвет в фокусе, серый в обычном состоянии. |
| `drawable/ic_*.xml` | Векторные иконки (логотип, календарь, Google, загрузка, «нет сети», успех…). |
| `drawable/bg_*.xml` | Фоны: точки онбординга, рамка загрузки фото, значок на аватаре. |
| `drawable/img_*.xml` | Картинки сплэша и онбординга (сейчас заглушки, заменить на PNG из Figma). |
| `xml/file_paths.xml` [Л2] | Папка, к которой FileProvider даёт доступ камере: `cacheDir/photos`. |

### Настройки и прочее

| Файл | За что отвечает |
|---|---|
| `AndroidManifest.xml` | Разрешения `INTERNET` и `ACCESS_NETWORK_STATE`, класс Application, все Activity (стартовая — `SplashActivity` с темой сплэша), `<provider>` FileProvider, диплинк `drivenext://login-callback` у `LoginActivity`. |
| `src/debug/AndroidManifest.xml` + `debug/res/xml/shortcuts.xml` | Только в debug-сборке: ярлык по долгому тапу на иконку «Запустить с нуля» — очищает данные, чтобы снова увидеть онбординг. |
| `app/build.gradle.kts` | minSdk 31, ViewBinding, `BuildConfig`-поля `SUPABASE_URL`/`SUPABASE_KEY` из `local.properties`, зависимости: core-splashscreen, viewpager2, material, lifecycle (ViewModel), supabase-kt (auth, postgrest, storage), ktor-client-android; плагин kotlinx.serialization. |
| `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties` | Общие настройки проекта и плагинов (AGP, Kotlin 2.2.21). |
| `local.properties` | Путь к SDK и ключи Supabase — **не попадает в Git**. |
| `supabase/schema.sql` [Л2] | SQL для Supabase: таблица `users`, приватный бакет `documents`, политики **RLS** (каждый видит только свои данные: `auth.uid() = id`). |
| `.gitignore` | Что не коммитить (build, .gradle, local.properties). |

---

## 6. Шпаргалка «что сказать за 1 минуту»

> «Это приложение DriveNext для аренды авто. Оно построено по чистой архитектуре: слои domain, data и presentation, плюс MVVM.
> В первой лабе я сделал запуск: SplashScreen API, проверку интернета через ConnectivityManager с экраном "Нет подключения" и онбординг на ViewPager2 с фрагментами. Флаг "онбординг пройден" хранится в SharedPreferences, а куда идти после сплэша решает GetStartDestinationUseCase.
> Во второй лабе — вход и регистрацию на Supabase. Вход по почте и через Google, сброс пароля. Регистрация — три фрагмента в одной Activity с общей ViewModel, поэтому данные не теряются при возврате назад и повороте. Дата выбирается через MaterialDatePicker, фото — через Activity Result API: PickVisualMedia для галереи и TakePicture с FileProvider для камеры. Данные уходят в Supabase Auth, фото — в Storage, профиль — в таблицу users.
> Ошибки Supabase переводятся в наши AppError в data-слое, поэтому экраны от Supabase не зависят. Ошибки сервера показываются диалогом, а при отсутствии интернета открывается экран "Нет подключения", после которого пользователь возвращается к той же форме».
