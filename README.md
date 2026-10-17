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
5. Crea Firestore y Storage. Las reglas de Firestore están versionadas en
   `firestore.rules`; consulta «Seguridad de Firebase» para desplegarlas. Configura
   también los índices que necesite el proyecto.
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
  obtiene el token de Google en presentación. Los ViewModels usan repositorios
  directamente para operaciones simples y casos de uso cuando hay validación u
  orquestación de varias operaciones.
- **Domain:** modelos, contratos en `domain/repository`, reglas de negocio y casos
  de uso con lógica propia. No importa Android, Firebase, DataStore ni clases de
  data.
- **Data:** implementaciones de los contratos, fuentes Firebase y mappers. Las
  observaciones de Firestore se exponen como `Flow` con `callbackFlow`; las lecturas
  puntuales usan `get().await()`. DataStore conserva la preferencia de sesión.
- **DI:** Hilt proporciona los SDK y enlaza contratos con implementaciones mediante
  `RepositoryModule` y `@Binds`.

Los contratos son `RecipeRepository`, `AuthRepository`, `UserRepository` y
`SessionRepository`. Sus implementaciones son `FirebaseRecipeRepository`,
`FirebaseAuthRepository`, `FirestoreUserRepository` y `DataStoreSessionRepository`.

Se mantienen cuatro casos de uso porque coordinan trabajo o aplican reglas:
`CreateRecipeUseCase` valida y construye la receta con el usuario actual;
`GoogleLoginUseCase` crea el perfil si es el primer acceso;
`RegisterUserUseCase` crea la cuenta y el perfil, y cierra la sesión; y
`LogoutUseCase` coordina el cierre de sesión y la limpieza de `rememberMe`.

El flujo de autenticación Google es:
Credential Manager → ID token → ViewModel → caso de uso → AuthRepository →
FirebaseAuthRepository → Firebase Authentication. Las credenciales de Firebase
se construyen únicamente en data.

## Comportamiento y persistencia

- El login con email exige un correo verificado y cierra la sesión si no lo está.
- El registro actualiza el nombre, envía el correo de verificación, crea el perfil
  y cierra la sesión. El primer acceso con Google crea el perfil si no existe.
- `rememberMe` se guarda en DataStore; Google lo activa al completar el login.
- Firestore observa recetas mediante `callbackFlow`; las consultas puntuales usan
  `get().await()`. Las imágenes se almacenan en Firebase Storage.
- Al crear y editar se recortan espacios exteriores y se descartan ingredientes
  y pasos vacíos, conservando el orden.
- La cámara utiliza una aplicación externa y FileProvider; uCrop realiza el
  recorte. Se conservan ese flujo y sus permisos.

## Formulario y validación de recetas

`RecipeFormStateHolder` mantiene el estado del formulario utilizado por ambos
ViewModels. Un solo `RecipeFormContent` presenta el formulario, mientras
`CreateRecipeViewModel` y `EditRecipeViewModel` mantienen separadas las operaciones
de creación y edición.
Sus routes conectan el estado y los eventos con la UI.

`RecipeFormValidator` valida entradas de presentación como la duración vacía o no
numérica y delega las reglas de receta a `RecipeValidator`. El dominio devuelve
`RecipeValidationError` y `RecipeValidationException`, sin textos localizados. La
UI traduce los errores tipados a recursos de `strings.xml`.

Al crear, el ViewModel valida, sube una imagen local si se seleccionó y llama a
`CreateRecipeUseCase`, que obtiene la identidad del autor, normaliza la receta y
la persiste. Si falla la persistencia posterior a una subida, intenta limpiar la
imagen huérfana sin reemplazar el error original. Al editar, el ViewModel carga
la receta puntualmente, permite conservar, reemplazar o quitar la imagen y
actualiza la receta existente.

Los errores de login se clasifican con `AuthError` y `AuthException`. Las pantallas
de login y registro emiten errores de UI tipados que la presentación resuelve a
recursos, sin mostrar mensajes crudos de Firebase.
Los estados se exponen con `StateFlow` y Compose los observa con
`collectAsStateWithLifecycle`.

Estos cambios no requieren migraciones, nuevas colecciones ni cambios del esquema
Firebase. Las recetas ya guardadas no se reescriben automáticamente.

## Seguridad de Firebase

Las reglas de Firestore están versionadas en `firestore.rules` y vinculadas desde
`firebase.json`. Exigen autenticación para leer recetas y limitan las escrituras
de recetas al UID propietario. Los perfiles solo se pueden crear con el UID y el
email autenticados; la lectura permite consultar el perfil propio por UID o por
el email de la cuenta, necesario para comprobar el primer acceso con Google.
Las consultas del cliente facilitan la UX y reducen datos descargados; las reglas
son las que autorizan realmente cada operación.

Para desplegar las reglas, autentícate con Firebase CLI, selecciona tu proyecto
Firebase y ejecuta desde la raíz del repositorio:

```bash
firebase deploy --only firestore:rules --project <project-id>
```

El despliegue es una acción manual del propietario del proyecto; no se ejecuta
desde Gradle ni desde la aplicación.

Las reglas de Firebase Storage no están incluidas en este cambio. Antes de
restringirlas, habrá que migrar las imágenes a rutas que incluyan el UID, por
ejemplo `recipes/{uid}/{uuid}.jpg`, y desplegar reglas de Storage acordes con esa
estructura.

## Testing y calidad

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Los tests unitarios cubren validación y estado del formulario, los ViewModels de
creación y edición, autenticación, mapeo de errores, persistencia, cancelación y
emisiones del listado. Usan repositorios fake y `kotlinx-coroutines-test`, sin
Firebase real ni red.

GitHub Actions ejecuta esos tres comandos al abrir o actualizar una pull request
y al hacer push a `master`. El repositorio debe tener configurado el secreto de
Actions `GOOGLE_SERVICES_JSON_BASE64`, con el `google-services.json` codificado
en Base64. El workflow no despliega la aplicación ni las reglas de Firebase.

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
