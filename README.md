## 🔑 Gemini API Key Setup

To run this app, you need a **Gemini API Key**.

### 1. Get a Gemini API Key

Create your API key from **Google AI Studio**.

### 2. Add the API Key to `local.properties`

Open the `local.properties` file in the root folder of the Android project and add:

```properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

For example:

```properties
GEMINI_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

### ⚠️ Important

* Do **not** commit your `local.properties` file to GitHub.
* Do **not** share your Gemini API key publicly.
* Make sure `local.properties` is included in your `.gitignore`.

After adding the API key, sync the Gradle project and run the application.
