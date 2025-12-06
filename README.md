# Virblog + WCFont

A Vert.x + Kotlin coroutines based blog platform with dynamic Chinese font subsetting.

## Features

### Blog Platform
- Multi-language blog with i18n support (zh-Hans, zh-Hant, en)
- Admin dashboard for content management
- Markdown content with syntax highlighting
- Tag-based categorization
- Blogroll management
- JWT-based authentication
- PostgreSQL database

### WCFont (Web Chinese Font Subsetter)
- Dynamic font subsetting based on page content
- WOFF format output for web optimization
- Caching of generated subsets
- Non-blocking I/O with Kotlin coroutines

## Requirements

- JDK 21+
- Gradle 8.x
- SQLite 3 (for local database file)

## Configuration

Edit `application.conf`:

```properties
# Font input directory (where .ttf files are located)
font.input=fonts/input

# Font output directory (where generated .woff files will be stored)
font.output=fonts/output

# Base URL for the service
url=http://localhost:8080

# Server port
port=8080

# Database configuration
db.path=data/virblog.db

# JWT secret key (change in production!)
jwt.key=your-secret-key

# Available locales
locales=zh-Hans,zh-Hant,en
```

## Database Setup

SQLite is used by default; the file path is `data/virblog.db`. Ensure the schema exists with the required tables:
- `users` - User accounts
- `posts` - Blog posts and pages (stores localized fields as JSON)
- `post_tags` - Tag definitions (localized JSON)
- `options` - Blog configuration (localized JSON)
- `blogrolls` - Blogroll links

## Build & Run

```bash
# Build
./gradlew build

# Run
./gradlew run

# Or build a fat JAR
./gradlew jar
java -jar build/libs/wcfont-1.0.0.jar
```

## URL Structure

### Blog Frontend
- `GET /` - Language redirect based on Accept-Language header
- `GET /{lang}/` - Blog index page
- `GET /{lang}/posts/{slug}` - Single post page
- `GET /{lang}/pages/{slug}` - Static page
- `GET /{lang}/tags/{slug}` - Posts by tag

### Admin Dashboard
- `GET /admin/` - Admin frontend (Angular.js)

### Admin API (`/api/v1/`)
- `POST /api/v1/login` - Login
- `GET /api/v1/logout` - Logout
- `GET /api/v1/user-info` - Get current user info
- `GET/POST/PUT/DELETE /api/v1/tags` - Tag management
- `GET/POST/PUT/DELETE /api/v1/posts` - Post management
- `GET/PUT /api/v1/options` - Blog options
- `PUT /api/v1/users/update-password` - Change password
- `PUT /api/v1/users/update` - Update user profile
- `GET/POST /api/v1/blogrolls` - Blogroll management
- `POST /api/v1/i18n/zhs2zht` - Simplified to Traditional Chinese conversion

### WCFont API

#### GET /wcfont/
Returns a JavaScript loader that dynamically loads font subsets.

**Query Parameters:**
- `s` - CSS selector for elements to apply the font
- `family` - Font family name (corresponds to TTF filename without extension)

**Example:**
```html
<script src="http://localhost:8080/wcfont/?s=.chinese-text&family=NotoSansSC"></script>
```

#### GET /wcfont/css
Returns CSS with `@font-face` declaration for the subsetted font.

**Query Parameters:**
- `family` - Font family name
- `content` - Text content to include in the subset

**Example:**
```
GET /wcfont/css?family=NotoSansSC&content=你好世界
```

#### GET /wcfont/fonts/{filename}
Serves the generated WOFF font files.

## Usage

1. Place your TTF font files in the `fonts/input` directory
2. Start the server
3. Include the loader script in your HTML:

```html
<script src="http://localhost:8080/wcfont/?s=.my-font&family=MyFont"></script>
<p class="my-font">这是中文内容</p>
```

The service will:
1. Scan elements matching the selector
2. Extract text content
3. Generate a font subset containing only the required characters
4. Apply the font to the elements
