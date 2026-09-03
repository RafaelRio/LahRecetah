# LahRecetah 🍳

Aplicación Android para **crear, descubrir y gestionar recetas de cocina**.

Desarrollada con **Kotlin** y **Jetpack Compose**, utilizando **Firebase** como backend e implementando una arquitectura basada en **MVVM**, separación por capas y casos de uso.

---

## ✨ Funcionalidades

* Registro e inicio de sesión con email y contraseña.
* Inicio de sesión con Google.
* Opción de mantener la sesión iniciada.
* Visualización de recetas publicadas.
* Consulta del detalle completo de una receta.
* Creación de recetas propias.
* Edición de recetas publicadas por el usuario.
* Eliminación de recetas con confirmación.
* Gestión de ingredientes y pasos de elaboración.
* Clasificación de recetas por categoría.
* Configuración de duración y dificultad.
* Selección y subida de imágenes.
* Perfil de usuario.
* Edición del nombre del perfil.
* Consulta de las recetas creadas por el usuario.
* Cierre de sesión.

---

## 🛠️ Tecnologías

| Tecnología                  | Uso                                  |
| --------------------------- | ------------------------------------ |
| **Kotlin**                  | Lenguaje principal                   |
| **Jetpack Compose**         | Interfaz de usuario                  |
| **Material 3**              | Sistema de diseño                    |
| **MVVM**                    | Arquitectura de presentación         |
| **Coroutines / Flow**       | Gestión asíncrona y reactiva         |
| **Hilt**                    | Inyección de dependencias            |
| **Navigation Compose**      | Navegación                           |
| **Firebase Authentication** | Autenticación de usuarios            |
| **Google Sign-In**          | Inicio de sesión con Google          |
| **Cloud Firestore**         | Almacenamiento de recetas y usuarios |
| **Firebase Storage**        | Almacenamiento de imágenes           |
| **DataStore**               | Preferencias de sesión               |
| **Coil**                    | Carga de imágenes                    |
| **Gradle Kotlin DSL**       | Configuración del proyecto           |

---

## 🧱 Arquitectura

El proyecto separa las responsabilidades en diferentes capas:

```text
com.rafario.lahrecetah
│
├── data/
│   ├── local/
│   ├── remote/
│   └── repository/
│
├── domain/
│   ├── mappers/
│   ├── model/
│   └── usecase/
│
├── di/
│
├── navigation/
│
├── ui/
│   ├── splash/
│   ├── login/
│   ├── register/
│   ├── main/
│   ├── recipe_list/
│   ├── recipe_detail/
│   ├── add_recipe/
│   ├── profile/
│   ├── custom_views/
│   └── theme/
│
└── MainActivity.kt
```

### Data

Gestiona las fuentes de datos de la aplicación:

* Firebase Authentication.
* Cloud Firestore.
* Firebase Storage.
* DataStore.
* Repositories.

### Domain

Contiene la lógica de negocio independiente de la interfaz:

* Modelos de dominio.
* Mappers.
* Casos de uso para recetas y usuarios.

### UI

Contiene las pantallas y `ViewModel` desarrollados con Jetpack Compose.

---

## 🔐 Autenticación

La aplicación utiliza **Firebase Authentication**.

Permite iniciar sesión mediante:

* Email y contraseña.
* Cuenta de Google.

También incluye registro de nuevos usuarios, cierre de sesión y gestión de la sesión actual.

La preferencia de mantener la sesión iniciada se almacena localmente mediante **DataStore**.

---

## 🍽️ Gestión de recetas

Cada receta contiene información como:

```kotlin
data class Recipe(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val createdByUid: String = "",
    val createdByName: String = "",
    val durationMinutes: Int = 0,
    val category: RecipeCategory = RecipeCategory.OTHER,
    val difficulty: Int = 1,
    val imageUrl: String = ""
)
```

Las recetas pueden clasificarse como:

* Entrante
* Primer plato
* Segundo plato
* Postre
* Dulce
* Ensalada
* Sopa
* Bebida
* Otro

---

## ➕ Crear y editar recetas

El formulario permite definir:

* Título.
* Descripción.
* Ingredientes.
* Pasos de elaboración.
* Categoría.
* Tiempo de preparación.
* Dificultad.
* Imagen.

Los ingredientes y pasos pueden añadirse o eliminarse dinámicamente.

Antes de guardar una receta se realizan diferentes validaciones, como comprobar que exista un título y al menos un ingrediente y un paso.

El mismo flujo permite también **editar recetas existentes**.

---

## 🖼️ Imágenes

Las imágenes seleccionadas para las recetas se suben a **Firebase Storage**.

Una vez finalizada la subida, la URL obtenida se almacena junto al resto de información de la receta en Firestore.

Las imágenes remotas se muestran en la interfaz utilizando **Coil**.

---

## 🔥 Firebase

Firebase actúa como backend principal de la aplicación:

```text
Firebase Authentication
        │
        ├── Usuarios y sesión
        │
Cloud Firestore
        │
        ├── Recetas
        └── Perfiles
        │
Firebase Storage
        │
        └── Imágenes de recetas
```

Las recetas se exponen mediante `Flow`, permitiendo que las pantallas reaccionen a los cambios de datos.

---

## 👤 Perfil

Desde el perfil el usuario puede:

* Consultar sus datos.
* Modificar su nombre.
* Ver todas las recetas que ha publicado.
* Editar una receta propia.
* Eliminar una receta.
* Cerrar sesión.

Las recetas del usuario se recuperan utilizando su `uid` de Firebase Authentication.

---

## 🧭 Navegación

La navegación principal utiliza **Navigation Compose**.

```text
Splash
  │
  ├── Login
  │     └── Register
  │
  └── Main
        │
        ├── Recetas
        ├── Añadir receta
        └── Perfil
              │
              └── Editar receta

Recetas
   │
   └── Detalle de receta
```

La pantalla principal utiliza una barra de navegación inferior con tres secciones:

1. Listado de recetas.
2. Crear receta.
3. Perfil.

---

## 💉 Inyección de dependencias

El proyecto utiliza **Hilt** para gestionar la inyección de dependencias.

Los `ViewModel`, repositories, fuentes de datos y servicios de Firebase se proporcionan mediante DI, reduciendo el acoplamiento entre las diferentes capas de la aplicación.

---

## ⚙️ Requisitos

* Android Studio
* Java 11
* `minSdk 30`
* `targetSdk 36`
* `compileSdk 36`
* Proyecto Firebase configurado

Servicios Firebase necesarios:

* Authentication
* Google Sign-In
* Cloud Firestore
* Firebase Storage

---

## 🚀 Instalación

Clona el repositorio:

```bash
git clone https://github.com/RafaelRio/LahRecetah.git
```

Accede al proyecto:

```bash
cd LahRecetah
```

Abre el proyecto con **Android Studio** y sincroniza Gradle.

Para compilar el APK de debug:

```bash
./gradlew assembleDebug
```

El APK generado estará disponible en:

```text
app/build/outputs/apk/debug/
```

---

## 🔥 Configuración de Firebase

Para utilizar tu propio proyecto de Firebase:

1. Crea un proyecto en Firebase.
2. Registra una aplicación Android con el package:

```text
com.rafario.lahrecetah
```

3. Configura:

   * Firebase Authentication.
   * Autenticación con Google.
   * Cloud Firestore.
   * Firebase Storage.

4. Descarga `google-services.json`.

5. Colócalo en:

```text
app/google-services.json
```

---

## 📌 Características técnicas destacadas

* **Kotlin + Jetpack Compose**
* **MVVM**
* Separación en capas `data`, `domain` y `ui`
* **Repository Pattern**
* **Use Cases**
* **Hilt**
* **Coroutines & Flow**
* **Firebase Authentication**
* **Google Sign-In**
* **Cloud Firestore**
* **Firebase Storage**
* **DataStore**
* **Navigation Compose**
* **Coil**

---

## 👨‍💻 Autor

Desarrollado por [Rafael Río](https://github.com/RafaelRio).

---

## 📄 Sobre el proyecto

Proyecto Android desarrollado como aplicación personal para practicar y aplicar una arquitectura escalable junto con herramientas modernas del ecosistema Android y servicios de Firebase.
