# LahRecetah 🍳

Aplicación Android para **crear, descubrir y gestionar recetas de cocina**.

Desarrollada con **Kotlin** y **Jetpack Compose**, utilizando **Firebase** como backend e implementando una arquitectura basada en **MVVM**, separación por capas y casos de uso.

> **Antes de ejecutarla:** esta versión utiliza Firebase como backend. Para configurar Authentication, Firestore, Storage y Google Sign-In en un entorno local, sigue la sección [Configuración de Firebase](#-configuración-de-firebase).
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
* JDK 17 o 21 para ejecutar Gradle (verificado localmente con JDK 17). El destino de bytecode Java/Kotlin está configurado en 11.
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

Abre el proyecto con **Android Studio**, selecciona un JDK compatible como Gradle JDK y sincroniza Gradle. Para ejecutar comandos en terminal, configura también `JAVA_HOME` con ese JDK.

Antes de probar autenticación y recetas, completa la [configuración de Firebase](#-configuración-de-firebase). El archivo incluido en el repositorio identifica un proyecto Firebase, pero no garantiza acceso a sus servicios ni autoriza nuevas firmas de debug.

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

### Compilar y ejecutar con tu propio backend

Cada persona que compile con un certificado distinto debe registrar esa firma para utilizar Google Sign-In. No es necesario registrar cada teléfono: se registra la firma del APK.

1. Crea un proyecto en Firebase.
2. Registra una aplicación Android con el package:

```text
com.rafario.lahrecetah
```

3. Obtén las huellas de la firma de debug desde la raíz del repositorio:

```bash
./gradlew :app:signingReport
```

En Windows, utiliza `gradlew.bat :app:signingReport`. Copia la **SHA-1 de la variante debug** y añádela en Firebase → Configuración del proyecto → Tus aplicaciones → aplicación Android → Huellas digitales del certificado.

4. En Authentication → Método de inicio de sesión, habilita **Correo electrónico/contraseña** y **Google**; completa el correo de soporte que solicite la consola.
5. Crea Cloud Firestore y Firebase Storage. Configura las reglas de acceso de acuerdo con la identidad y propiedad de los datos. Las reglas e índices se gestionan directamente desde el proyecto Firebase y deben estar configurados antes de ejecutar la aplicación.
6. Descarga el `google-services.json` actualizado después de configurar Google y la firma. Sustituye el archivo situado en:

```text
app/google-services.json
```

7. Sincroniza Gradle y vuelve a compilar/ejecutar la aplicación.

Para el acceso con email, verifica el correo recibido antes de iniciar sesión. Durante el registro, el perfil del usuario se guarda en Firestore mientras la sesión sigue autenticada. Una vez completado correctamente el alta, la aplicación cierra la sesión para exigir la verificación del correo antes del primer acceso.
Consulta la [guía oficial de Google Sign-In con Firebase](https://firebase.google.com/docs/auth/android/google-signin).

### Si Google muestra error 10

Comprueba que el proyecto Firebase, el package `com.rafario.lahrecetah`, el cliente OAuth y la SHA-1 corresponden al APK que estás ejecutando. Una firma creada en otro ordenador puede tener una SHA-1 diferente. Después de registrar la firma, descarga la configuración actualizada y recompila.

El código utiliza `default_web_client_id` para solicitar el token de Google: debe ser el cliente OAuth **web**, no el identificador del cliente Android.

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
