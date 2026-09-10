# LahRecetah

Aplicación Android de recetas con Kotlin y Jetpack Compose. Permite consultar,
crear, editar y eliminar recetas propias, gestionar el perfil e iniciar sesión
con email o Google. Las imágenes pueden seleccionarse de la galería o capturarse
con la cámara y recortarse con uCrop.

## Requisitos

- Android Studio y JDK 17 para Gradle. El bytecode Java/Kotlin utiliza el destino 11.
- Android SDK 36; la aplicación requiere Android 11 (API 30) o posterior.
- Un proyecto Firebase con Authentication, Firestore y Storage.

Se mantiene un único módulo Android, `app`.

## Configuración local

1. Clona el repositorio y ábrelo en Android Studio.
2. Registra en Firebase una aplicación Android con el package
   `com.rafario.lahrecetah`.
3. Habilita los proveedores Email/contraseña y Google en Authentication.
4. Ejecuta `./gradlew :app:signingReport` y registra las huellas SHA-1 y SHA-256
   de tu certificado en la configuración de la aplicación Firebase.
5. Crea Firestore y Storage y configura sus reglas e índices para permitir las
   operaciones correspondientes a cada usuario.
6. Descarga tu propio `google-services.json` actualizado y colócalo en
   `app/google-services.json`. **Cada desarrollador debe proporcionar el suyo**;
   Git ignora este archivo.
7. Sincroniza Gradle y ejecuta la aplicación.

Google Sign-In usa Credential Manager y el cliente OAuth **web** generado como
`default_web_client_id`, no el cliente Android. El package, las firmas y el proyecto
Firebase deben corresponder al APK instalado. Si cambias firmas o habilitas Google
después de descargar el JSON, vuelve a descargarlo.

En Windows utiliza `gradlew.bat` en lugar de `./gradlew`.

```bash
./gradlew assembleDebug
```

El APK se genera en `app/build/outputs/apk/debug/`.

## Arquitectura

MVVM con separación por capas y casos de uso donde aportan lógica de aplicación;
no pretende ser una implementación estricta de Clean Architecture.

- **UI:** pantallas Compose, estados y eventos de ViewModels. Credential Manager
  obtiene el token de Google en presentación. Los ViewModels dependen de casos
  de uso y contratos del dominio.
- **Domain:** modelos, contratos en `domain/repository`, casos de uso y reglas de
  validación. No importa Android, Firebase, DataStore ni clases de data.
- **Data:** implementaciones de los contratos, fuentes Firebase y mapper de
  Firestore. DataStore conserva la preferencia de sesión.
- **DI:** Hilt proporciona los SDK y enlaza contratos con implementaciones mediante
  `RepositoryModule` y `@Binds`.

Los contratos son `RecipeRepository`, `AuthRepository`, `UserRepository` y
`SessionRepository`. Sus implementaciones son `FirebaseRecipeRepository`,
`FirebaseAuthRepository`, `FirestoreUserRepository` y `DataStoreSessionRepository`.

El flujo de autenticación Google es:
Credential Manager → ID token → ViewModel → caso de uso → AuthRepository →
FirebaseAuthRepository → Firebase Authentication. Las credenciales de Firebase
se construyen únicamente en data.

## Comportamiento y persistencia

- El login con email exige un correo verificado y cierra la sesión si no lo está.
- El registro actualiza el nombre, envía el correo de verificación, crea el perfil
  y cierra la sesión. El primer acceso con Google crea el perfil si no existe.
- `rememberMe` se guarda en DataStore; Google lo activa al completar el login.
- Firestore expone recetas mediante Flow; las imágenes se almacenan en Storage.
- `RecipeValidator` centraliza título, descripción, ingredientes, pasos, duración
  positiva y dificultad entre 1 y 5. El formulario convierte la duración textual
  y conserva sus mensajes de validación.
- Al crear y editar se recortan espacios exteriores y se descartan ingredientes
  y pasos vacíos, conservando el orden.
- La cámara utiliza una aplicación externa y FileProvider; uCrop realiza el
  recorte. Se conservan ese flujo y sus permisos.

Estos cambios no requieren migraciones, nuevas colecciones ni cambios del esquema
Firebase. Las reglas e índices del backend siguen gestionándose en Firebase.
Las recetas ya guardadas no se reescriben automáticamente.

## Testing y calidad

```bash
./gradlew testDebugUnitTest
./gradlew lint
./gradlew assembleDebug
```

Los tests unitarios cubren el formulario, la validación y normalización al crear
recetas, la identidad del autor, los errores de persistencia, los eventos y estados
del login, las escrituras de preferencias, los intentos simultáneos y las emisiones
del listado. Incluyen casos de cancelación. Usan repositorios fake y
`kotlinx-coroutines-test`, sin Firebase real ni red.

La prueba manual de autenticación, verificación de email, cámara, galería y recorte
requiere un dispositivo o emulador y la configuración Firebase propia.

## Decisiones técnicas

- **Firebase:** resuelve autenticación, datos compartidos e imágenes sin mantener
  un servidor propio para esta aplicación.
- **Flow y StateFlow:** permiten observar cambios de Firestore y representar el
  estado de pantalla de forma reactiva, integrándose con coroutines y Compose.
- **Repositories mediante interfaces:** separan las operaciones que necesita la
  aplicación de los SDK y permiten sustituir Firebase por fakes en tests.
- **Hilt:** construye y comparte dependencias sin que las pantallas o los casos de
  uso tengan que conocer cómo se inicializan los servicios.

## Referencias

- [Google Sign-In con Credential Manager](https://developer.android.com/identity/sign-in/credential-manager-siwg-implementation)
- [Autenticación Google con Firebase](https://firebase.google.com/docs/auth/android/google-signin)

## Autor

[Rafael Río](https://github.com/RafaelRio)
