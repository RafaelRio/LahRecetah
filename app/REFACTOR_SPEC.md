# LahRecetah — Specification de refactor guiado, seguridad y calidad

**Repositorio:** `RafaelRio/LahRecetah`  
**Base revisada:** `master` en `c2e6c655c18121b69a82bd9299fba57aad6bbf94`  
**Objetivo:** definir el estado final deseado de las mejoras arquitectónicas, de fiabilidad, seguridad y testing detectadas durante la revisión técnica del proyecto. La implementación se realizará manualmente por el desarrollador, guiado por el agente de forma incremental, un micro-paso cada vez.

---

# 0. Contrato de ejecución para el agente

Este documento define **qué estado final debe alcanzar el proyecto** y qué restricciones técnicas deben respetarse. No autoriza una implementación autónoma de todas las fases.

Las secciones llamadas “Fase” agrupan cambios relacionados por objetivo arquitectónico. **No deben interpretarse como unidades de ejecución obligatorias ni como permiso para implementarlas completas de una sola vez.** El agente debe descomponerlas en pasos pequeños, revisables y aprobables.

## 0.1 Modo de trabajo obligatorio: refactor guiado

El trabajo será interactivo y dirigido por el desarrollador.

Antes de modificar cualquier archivo, el agente debe:

1. Leer esta specification completa.
2. Analizar el estado actual real del proyecto.
3. Comparar el código actual con esta specification.
4. Detectar contradicciones, riesgos, decisiones obsoletas o puntos ambiguos.
5. Proponer un plan completo dividido en pasos pequeños y ordenados.
6. Detenerse y esperar aprobación explícita.

**No se permite modificar código durante esta primera fase de análisis y planificación.**

## 0.2 Granularidad del plan

Cada paso propuesto debe ser lo bastante pequeño como para que:

- tenga un único objetivo principal;
- pueda explicarse claramente antes de implementarse;
- afecte al menor número razonable de archivos;
- sea revisable mediante un diff pequeño;
- deje preferiblemente el proyecto compilando;
- permita ejecutar tests relevantes;
- pueda revertirse de forma aislada si fuese necesario.

Ejemplos de granularidad correcta:

```text
Eliminar GetRecipesUseCase y adaptar RecipeListViewModel + sus tests
```

```text
Añadir getRecipeById one-shot al data source
```

```text
Exponer getRecipeById en RecipeRepository y migrar delete/update
```

Ejemplo de granularidad demasiado grande:

```text
Refactorizar toda la arquitectura y todos los ViewModels
```

El agente puede dividir una “Fase” de esta specification en tantos pasos como sean necesarios.

## 0.3 Evitar trabajo temporal

El agente debe evitar implementar funcionalidad nueva en una clase que está previsto eliminar pocos pasos después si esa funcionalidad puede implementarse directamente en la clase definitiva.

Se permiten **adaptaciones mínimas temporales** únicamente cuando sean necesarias para mantener el proyecto compilando entre pasos.

Ejemplo concreto de esta specification:

- al cambiar `uploadRecipeImage()` de `String` a `Result<String>`, `AddRecipeViewModel` puede adaptarse mínimamente para seguir compilando;
- **no** se debe añadir en `AddRecipeViewModel` la nueva lógica completa de cleanup de imágenes huérfanas si `CreateRecipeViewModel` y `EditRecipeViewModel` van a sustituirlo inmediatamente después;
- esa lógica nueva debe nacer directamente en los ViewModels definitivos.

## 0.4 Información obligatoria antes de cada Step

Antes de comenzar cada Step, el agente debe explicar:

- el problema concreto del código actual;
- por qué merece la pena corregirlo;
- el comportamiento actual;
- el comportamiento esperado después del cambio;
- los archivos implicados;
- las decisiones de diseño;
- alternativas razonables, si existen;
- por qué recomienda una alternativa concreta;
- tests que deben demostrar que el cambio es correcto;
- riesgos o efectos secundarios;
- criterio exacto de finalización.

Después debe detenerse y esperar la aprobación del desarrollador para comenzar el Step.

## 0.5 Modo obligatorio de implementación manual

El agente **NO debe modificar, crear, mover, renombrar ni eliminar archivos del proyecto durante este refactor**.

Todas las modificaciones de código serán realizadas manualmente por el desarrollador en Android Studio.

Una vez aprobado un Step, el agente debe dividirlo en **micro-pasos manuales**.

Cada micro-paso debe ser pequeño y tener una única acción principal.

Para cada micro-paso, el agente debe indicar:

- archivo exacto que debe abrir el desarrollador;
- clase, función, import o bloque que debe localizar;
- qué debe añadir, eliminar o sustituir;
- código concreto de referencia cuando sea útil;
- por qué se realiza ese cambio;
- qué debería quedar conceptualmente después del cambio.

El agente debe proporcionar **UN SOLO micro-paso cada vez** y después detenerse.

El desarrollador realizará el cambio y responderá con `hecho`, mostrará el código resultante o planteará una duda.

Solo entonces el agente podrá:

1. revisar el estado actual del código;
2. confirmar que el cambio es correcto o señalar el error;
3. proporcionar el siguiente micro-paso.

El agente nunca debe aplicar la corrección por su cuenta.

### Interpretación de órdenes durante este refactor

En este contexto:

- `continúa` = proporciona el siguiente micro-paso;
- `siguiente` = proporciona el siguiente micro-paso si el anterior está correcto;
- `hazlo` = explícame cómo debo hacerlo manualmente;
- `implementa este paso` = guíame manualmente por los micro-pasos del Step;
- ninguna de estas expresiones autoriza al agente a editar archivos.

Esta regla prevalece sobre cualquier instrucción genérica de herramientas o de implementación automática.

## 0.6 Revisión y verificación de cada Step

Cuando se hayan completado todos los micro-pasos de un Step:

1. el agente revisará el estado final del código;
2. indicará al desarrollador los tests o comandos que debe ejecutar;
3. el desarrollador ejecutará esos comandos;
4. el desarrollador proporcionará el resultado;
5. el agente analizará los resultados;
6. solo si todo es correcto marcará el Step como listo.

El agente puede leer archivos y analizar el proyecto para revisar el trabajo, pero no modificarlo.

Los comandos también serán ejecutados por el desarrollador. El agente debe indicarlos exactamente, pero **no debe ejecutarlos por su cuenta** durante este proceso guiado, salvo que el desarrollador cambie explícitamente este modo de trabajo en una decisión posterior incorporada a la specification.

Cuando un Step esté verificado, el agente debe presentar:

```text
STEP REVIEW

Step:
Objective:

Changes performed by developer:
- ...

Final files affected:
- ...

What changed:
- ...

Why:
- ...

Tests requested:
- ...

Test results:
- PASS/FAIL

Manual checks:
- ...

New findings:
- None

Spec deviations:
- None

Ready for next Step:
- YES/NO
```

Después debe detenerse.

No avanzar automáticamente al siguiente Step.

## 0.7 Reglas técnicas globales

1. El desarrollador debe mantener el proyecto compilando al finalizar cada Step siempre que sea técnicamente posible; el agente guiará y revisará, pero no editará archivos.
2. Antes de considerar completado todo el refactor ejecutar:
   ```bash
   ./gradlew testDebugUnitTest lintDebug assembleDebug
   ```
3. No introducir nuevas arquitecturas globales: no migrar a MVI, Redux, KMP, Room, multi-module ni otra solución no pedida.
4. Mantener Hilt, Jetpack Compose, Firebase Authentication, Firestore, Storage y DataStore.
5. Mantener un único módulo Gradle `app`.
6. No desplegar reglas de Firebase automáticamente. Crear/versionar los archivos de reglas y documentar el comando de despliegue, pero el despliegue real requiere credenciales del propietario.
7. No convertir `CancellationException` en un error funcional. Debe propagarse.
8. No hacer un único refactor monolítico.
9. Si el agente crea commits, usar commits pequeños y coherentes con los pasos aprobados; no es obligatorio crear commits.
10. Si una operación externa no puede completarse por falta de credenciales, no inventar credenciales ni sustituir el diseño.
11. Esta specification prevalece sobre preferencias genéricas del agente, salvo que el código real demuestre que una instrucción es incompatible o incorrecta; en ese caso debe detenerse y explicarlo.

## 0.8 Protocolo ante ambigüedad o conflicto

El agente **no debe rellenar silenciosamente huecos de diseño importantes**.

Debe detenerse antes de modificar código si ocurre cualquiera de estos casos:

- la specification contradice el estado real del proyecto;
- un requisito produciría una regresión funcional;
- una API o dependencia disponible no permite implementar literalmente lo indicado;
- dos decisiones de la specification entran en conflicto;
- el cambio requiere una migración de datos no contemplada;
- el cambio requiere credenciales, infraestructura o acciones externas no disponibles;
- hay más de una solución con consecuencias arquitectónicas distintas y la specification no fija cuál usar.

En ese caso debe responder:

```text
SPEC CONFLICT

Relevant requirement:
...

Current code / constraint:
...

Why they conflict:
...

Options:
A. ...
B. ...

Recommended option:
...

No code has been changed for this decision.
```

y esperar decisión.

## 0.9 Orden general

Las fases de esta specification expresan el orden lógico preferente, pero el agente debe transformarlas en un plan granular antes de empezar.

Puede proponer pequeños ajustes de orden cuando reduzcan riesgo o eviten cambios temporales innecesarios, pero debe explicarlos y obtener aprobación en el plan inicial.

Para la parte de formulario, validación y separación de ViewModels, el orden conceptual aprobado es:

```text
RecipeFormState / RecipeFormStateHolder
        ↓
validación tipada de formulario y dominio
        ↓
CreateRecipeViewModel
        ↓
EditRecipeViewModel
        ↓
CreateRecipeRoute / EditRecipeRoute + RecipeFormContent compartido
        ↓
eliminación de AddRecipeViewModel
```

---

# 1. Objetivos funcionales y arquitectónicos

El resultado debe conservar estas capacidades:

- login con email/contraseña;
- login con Google mediante Credential Manager;
- registro;
- remember-me mediante DataStore;
- listado de recetas;
- detalle de receta;
- creación de receta;
- edición de receta propia;
- borrado de receta propia;
- perfil y listado de recetas propias;
- subida de imágenes a Firebase Storage;
- observación reactiva de Firestore mediante `Flow`.

Además, la implementación debe cumplir estas decisiones.

## 1.1 Clean Architecture pragmática

El proyecto seguirá estando **inspirado en Clean Architecture**, no será una implementación estricta.

- `ui`: Compose, ViewModels, estado de formulario, eventos, errores de presentación y validación puramente de presentación.
- `domain`: modelos, contratos, errores de dominio, reglas de negocio y casos de uso que realmente añadan lógica u orquestación.
- `data`: Firebase, DataStore, mappers e implementaciones de repositorios.
- `di`: Hilt.

## 1.2 Regla para casos de uso

Un caso de uso solo debe existir si hace al menos una de estas cosas:

- aplica reglas de negocio;
- combina varias dependencias;
- transforma datos con significado de aplicación;
- coordina varias operaciones;
- representa una acción de aplicación con lógica propia.

No debe existir un use case que únicamente delegue una llamada 1:1 al repositorio.

## 1.3 Regla de errores y cancelación

- `CancellationException` siempre se relanza.
- Una cancelación no debe producir `Error` de UI.
- Los repositorios que devuelvan `Result<T>` no deben encapsular voluntariamente una `CancellationException` dentro de `Result.failure`.
- Los consumidores seguirán siendo defensivos ante un `Result.failure(CancellationException)` para evitar convertirlo en error si un fake o implementación futura rompe accidentalmente el contrato.
- Los textos visibles al usuario se resolverán en presentación mediante recursos; dominio y data no deben contener textos localizados destinados a UI.

---

> **Nota de ejecución:** a partir de aquí, las “Fases” describen grupos de requisitos del estado final. El agente no debe ejecutar una fase completa automáticamente. Debe convertir estas fases en pasos pequeños según el protocolo de la sección 0 y esperar aprobación antes de cada implementación.

# 2. Fase 1 — Eliminar casos de uso sin valor

## 2.1 Eliminar estos archivos

Eliminar:

```text
domain/usecase/recipes/GetRecipesUseCase.kt
domain/usecase/recipes/ObserveRecipeByIdUseCase.kt
domain/usecase/users/LoginUserUseCase.kt
domain/usecase/users/SaveRememberMeUseCase.kt
domain/usecase/users/GetRememberMeUseCase.kt
domain/usecase/users/ClearSessionUseCase.kt
```

## 2.2 Mantener estos casos de uso

Mantener:

```text
CreateRecipeUseCase
GoogleLoginUseCase
RegisterUserUseCase
LogoutUseCase
```

Motivo:

- `CreateRecipeUseCase`: valida dominio, obtiene usuario, construye/normaliza receta y persiste.
- `GoogleLoginUseCase`: autentica y crea perfil si es primer acceso.
- `RegisterUserUseCase`: autentica/crea usuario, crea perfil y cierra sesión.
- `LogoutUseCase`: coordina Auth + limpieza de sesión.

## 2.3 Cambios exactos de dependencias

### `RecipeListViewModel`

Cambiar `GetRecipesUseCase` por `RecipeRepository`.

Origen:

```kotlin
recipeRepository.observeRecipes()
```

### `RecipeDetailViewModel`

Cambiar `ObserveRecipeByIdUseCase` por `RecipeRepository`.

Mantener:

```kotlin
recipeRepository.observeRecipeById(recipeId)
```

No sustituir el detalle reactivo por lectura puntual.

### `LoginViewModel`

Inyectar:

```text
AuthRepository
GoogleLoginUseCase
SessionRepository
```

Email:

```kotlin
authRepository.login(email, password)
```

Google sigue usando `GoogleLoginUseCase`.

Remember-me:

```kotlin
sessionRepository.saveRememberMe(value)
```

### `SplashViewModel`

Inyectar `SessionRepository` y `AuthRepository`.

Usar `sessionRepository.rememberMeFlow` con el usuario actual.

No hacer reactivo `AuthRepository.getCurrentUser()` en esta tarea.

### `LogoutUseCase`

Depender directamente de `AuthRepository` y `SessionRepository`:

```kotlin
authRepository.logout()
sessionRepository.clear()
```

No depender de `ClearSessionUseCase`.

## 2.4 Acceptance criteria

- no quedan referencias a los seis casos de uso eliminados;
- los ViewModels compilan con repositorios directos donde corresponde;
- los cuatro use cases con responsabilidad real permanecen;
- tests adaptados y pasando.

---

# 3. Fase 2 — Corregir semántica de `RecipeListViewModel`

Eliminar `onStart { emit(Loading) }`.

Mantener:

```kotlin
initialValue = RecipeListUiState.Loading
```

y:

```kotlin
SharingStarted.WhileSubscribed(5_000)
```

Mantener `catch` antes de `stateIn`.

Comportamiento:

```text
Primera entrada:
Loading -> Success

Reentrada:
Success retenido -> Success actualizado
```

Acceptance criteria:

- no `onStart`;
- primer valor `Loading`;
- reentrada sin `Loading` artificial;
- cancelación propagada.

---

# 4. Fase 3 — Lectura puntual de recetas

## 4.1 Data source

Añadir:

```kotlin
suspend fun getRecipeById(recipeId: String): Recipe?
```

Con:

```kotlin
firestore.collection("recipes")
    .document(recipeId)
    .get()
    .await()
```

`null` si no existe; `toRecipe()` si existe.

No listener.

## 4.2 Repository contract

Añadir:

```kotlin
suspend fun getRecipeById(recipeId: String): Result<Recipe?>
```

## 4.3 Repository implementation

- éxito -> `Result.success`;
- cancelación -> relanzar;
- resto -> `Result.failure`.

## 4.4 Sustituir one-shot

Delete/update usan `dataSource.getRecipeById`.

`EditRecipeViewModel` usa `recipeRepository.getRecipeById`.

El detalle sigue usando `observeRecipeById`.

## 4.5 `null`

Delete con lectura `null`: intentar delete igualmente; si Firestore confirma, éxito.

Update con lectura `null`: devolver fallo controlado de receta inexistente y no hacer update.

## 4.6 Acceptance criteria

- no `observeRecipeById(...).first()` para one-shot;
- detalle sigue reactivo.

---

# 5. Fase 4 — API consistente de imágenes y semántica de Storage

## 5.1 Contrato

Cambiar:

```kotlin
suspend fun uploadRecipeImage(uri: String): Result<String>
```

Añadir:

```kotlin
suspend fun deleteRecipeImage(imageUrl: String): Result<Unit>
```

## 5.2 Subida

- éxito -> URL en `Result.success`;
- cancelación -> relanzar;
- resto -> `Result.failure`.

## 5.3 Borrado de imagen

- blank -> éxito;
- éxito -> éxito;
- cancelación -> relanzar;
- resto -> failure.

## 5.4 Adaptación temporal del ViewModel antiguo

Si `AddRecipeViewModel` sigue existiendo al cambiar el contrato, adaptarlo solo para compilar y consumir `Result<String>`.

No implementar ahí el cleanup nuevo completo de huérfanos.

## 5.5 Delete recipe

1. leer receta;
2. guardar imageUrl;
3. borrar Firestore;
4. si falla Firestore -> fallo;
5. si éxito -> cleanup Storage;
6. fallo ordinario cleanup -> delete sigue siendo éxito;
7. cancelación -> propagar.

## 5.6 Update recipe

1. leer receta actual;
2. guardar oldImageUrl;
3. actualizar Firestore;
4. si éxito y cambió URL -> cleanup antigua;
5. fallo ordinario cleanup -> update sigue siendo éxito;
6. cancelación -> propagar.

## 5.7 Imagen nueva huérfana

La lógica se implementa directamente en `CreateRecipeViewModel` y `EditRecipeViewModel`.

Si la persistencia posterior falla:

- intentar borrar la nueva imagen;
- cleanup best-effort;
- no sustituir error original;
- con cancelación, `NonCancellable` solo para cleanup y después relanzar.

No rollback de Firestore confirmado.

## 5.8 Acceptance criteria

- operaciones de imagen con `Result`;
- no consumidor de String directo;
- delete/update no fallan por cleanup ordinario posterior;
- create/edit limpian nueva imagen huérfana;
- esa lógica vive en ViewModels definitivos.

---

# 6. Fase 5 — Estado de formulario compartido por composición

Crear:

```text
ui/recipe_form/RecipeFormState.kt
ui/recipe_form/RecipeFormStateHolder.kt
```

## 6.1 State

Campos:

```text
title
description
durationText
ingredients
steps
difficulty
category
existingImageUrl
selectedLocalImageUri
focusedIngredientIndex
focusedStepIndex
```

Semántica:

- `existingImageUrl`: remota;
- `selectedLocalImageUri`: local nueva;
- preview: local > remota > ninguna;
- no `startsWith("http")`.

Eliminar imagen:

```text
existingImageUrl = null
selectedLocalImageUri = null
```

## 6.2 Holder

Clase normal, no ViewModel.

Responsabilidades:

- MutableStateFlow privado;
- StateFlow público;
- cambios de campos;
- ingredientes;
- pasos;
- foco;
- imagen;
- populate;
- reset.

No contener loading, events, edit mode, recipe ID ni errores de persistencia.

## 6.3 Acceptance criteria

- holder y state existen;
- composición, no herencia;
- tests del holder;
- sin inferencia HTTP.

---

# 7. Fase 6 — Validación tipada de formulario y dominio

Debe hacerse antes de crear los nuevos ViewModels.

## 7.1 Domain error

```kotlin
enum class RecipeValidationError {
    TITLE,
    DESCRIPTION,
    INGREDIENTS,
    STEPS,
    DURATION,
    DIFFICULTY
}
```

Sin `message`, español, recursos Android ni strings UI.

## 7.2 Domain exception

Crear:

```kotlin
class RecipeValidationException(
    val reason: RecipeValidationError
) : Exception()
```

## 7.3 RecipeValidator

Mantener reglas actuales y devolver `RecipeValidationError?`.

## 7.4 CreateRecipeUseCase

Si la validación falla:

```kotlin
return Result.failure(
    RecipeValidationException(error)
)
```

No `IllegalArgumentException(error.message)`.

## 7.5 RecipeFormValidator

Mover a:

```text
ui/recipe_form/RecipeFormValidator.kt
```

No devolver String ni recurso Android.

## 7.6 Form error

Crear:

```kotlin
sealed interface RecipeFormValidationError {
    data object DurationRequired : RecipeFormValidationError
    data object DurationNotInteger : RecipeFormValidationError

    data class Domain(
        val reason: RecipeValidationError
    ) : RecipeFormValidationError
}
```

## 7.7 Form result

```kotlin
sealed interface RecipeFormValidationResult {
    data class Valid(
        val durationMinutes: Int
    ) : RecipeFormValidationResult

    data class Invalid(
        val error: RecipeFormValidationError
    ) : RecipeFormValidationResult
}
```

Reglas:

1. blank -> DurationRequired;
2. parse null -> DurationNotInteger;
3. si parsea -> RecipeValidator;
4. error dominio -> Domain(error);
5. válido -> Valid(duration).

## 7.8 UI texts

Compose traduce tipos a `strings.xml`.

Ningún validator o error de domain llama a `stringResource`.

## 7.9 Acceptance criteria

- validator de formulario fuera de domain;
- domain sin textos;
- exception tipada;
- use case con reason;
- duración vacía/no numérica/dominio diferenciadas.

---

# 8. Fase 7 — Separar creación y edición de receta

## 8.1 CreateRecipeViewModel

Crear en `ui/add_recipe/`.

Dependencias:

```text
CreateRecipeUseCase
RecipeRepository
```

Usa `RecipeFormStateHolder`.

Responsabilidades:

- cambios de formulario;
- loading;
- validar;
- subir imagen local;
- CreateRecipeUseCase;
- cleanup de nueva imagen si persistencia falla;
- reset en éxito;
- evento Created;
- errores tipados.

Sin lógica de edición.

## 8.2 EditRecipeViewModel

Crear en `ui/edit_recipe/`.

Dependencia principal: `RecipeRepository`.

Responsabilidades:

- recipeId;
- getRecipeById;
- populate holder;
- loading/ready;
- validar;
- calcular imagen final;
- upload nueva;
- construir Recipe;
- normalized;
- update;
- cleanup nueva si update falla;
- Updated;
- errores tipados.

No crear UpdateRecipeUseCase sin lógica real.

Regla de imagen final:

1. selectedLocalImageUri -> subir y usar URL;
2. si no, existingImageUrl;
3. si ambas null -> `""`.

## 8.3 Error tipado de formulario

Crear o equivalente:

```kotlin
sealed interface RecipeFormUiError {
    data class Validation(
        val error: RecipeFormValidationError
    ) : RecipeFormUiError

    data object RecipeNotFound : RecipeFormUiError
    data object ImageUploadFailed : RecipeFormUiError
    data object PersistenceFailed : RecipeFormUiError
    data object Unexpected : RecipeFormUiError
}
```

No exponer `Throwable.message` Firebase como UI.

## 8.4 AddRecipeScreen selector

Mantener firma pública y seleccionar:

```text
null -> CreateRecipeRoute
id   -> EditRecipeRoute
```

Son composables route, no destinos NavHost.

## 8.5 Routes

`CreateRecipeRoute` y `EditRecipeRoute`:

- hiltViewModel;
- collect lifecycle;
- eventos;
- mapear errores a recursos;
- usar el mismo `RecipeFormContent`.

Edit route inicia carga y llama a `onEditFinished()` en Updated.

## 8.6 RecipeFormContent

Único formulario visual.

Recibe state + callbacks; no ViewModels ni navegación.

No duplicar.

## 8.7 Preservar UI existente

Mantener:

- galería;
- cámara;
- permisos;
- FileProvider;
- uCrop;
- preview;
- foco;
- toasts/eventos equivalentes.

## 8.8 Eliminar AddRecipeViewModel

Solo al final de esta migración, cuando ambos VMs estén probados y UI compile.

## 8.9 Acceptance criteria

- 2 VMs;
- holder compartido;
- sin Base VM;
- único formulario;
- routes no NavHost destinations;
- AddRecipeViewModel eliminado;
- sin startsWith("http");
- cleanup en VMs definitivos;
- UI funcional preservada.

---

# 9. Fase 8 — Errores de autenticación tipados

## 9.1 Domain

Crear `AuthError`:

```kotlin
enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_NOT_VERIFIED,
    NETWORK,
    USER_DISABLED,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    UNKNOWN
}
```

Crear `AuthException(error, cause)`.

Sin textos.

## 9.2 Email no verificado

No comparar texto.

Crear marcador tipado interno de data, por ejemplo:

```kotlin
internal class EmailNotVerifiedException : Exception()
```

`FirebaseAuthDataSource` lo usa cuando el usuario no está verificado y hace signOut como ahora.

## 9.3 Mapper

Mapeo:

```text
EmailNotVerifiedException -> EMAIL_NOT_VERIFIED
FirebaseNetworkException -> NETWORK
FirebaseAuthInvalidCredentialsException -> INVALID_CREDENTIALS
FirebaseAuthInvalidUserException + errorCode == "ERROR_USER_DISABLED" -> USER_DISABLED
otro FirebaseAuthInvalidUserException -> INVALID_CREDENTIALS
FirebaseAuthUserCollisionException -> EMAIL_ALREADY_IN_USE
FirebaseAuthWeakPasswordException -> WEAK_PASSWORD
otro -> UNKNOWN
```

CancellationException se relanza antes.

No comparar mensajes.

## 9.4 Repository

Los métodos Auth devuelven `Result<AuthUser>` con `AuthException` en fallos no cancelables.

No escapar Firebase exceptions crudas.

## 9.5 Login UI errors

Definir:

```kotlin
sealed interface LoginUiError {
    data object MissingCredentials : LoginUiError

    data class Auth(
        val reason: AuthError
    ) : LoginUiError

    data object SessionPersistence : LoginUiError
    data object Unexpected : LoginUiError
}
```

`LoginEvent.Error` transporta `LoginUiError`.

- empty -> MissingCredentials;
- AuthException -> Auth;
- rememberMe failure -> SessionPersistence;
- otro -> Unexpected;
- cancel -> sin evento.

## 9.6 Register UI errors

Definir:

```kotlin
sealed interface RegisterUiError {
    data object MissingFields : RegisterUiError

    data class Auth(
        val reason: AuthError
    ) : RegisterUiError

    data object Unexpected : RegisterUiError
}
```

`RegisterEvent.Error` transporta ese tipo.

## 9.7 Textos

Compose resuelve todos a recursos.

No mostrar:

```text
FirebaseException.message
Throwable.message
AuthException.message
```

## 9.8 Google

AuthException conserva AuthError.

Fallos de perfil Firestore no se falsifican como invalid credentials: se tratan como Unexpected.

## 9.9 Tests

Cubrir:

- email not verified;
- network;
- invalid credentials;
- user disabled por errorCode;
- otro invalid user;
- collision;
- weak password;
- unknown;
- cancellation.

Mantener/adaptar el test de cancellation in result.

## 9.10 Acceptance criteria

- clasificación por tipos/códigos;
- marcador tipado email;
- user disabled por errorCode;
- ViewModels exponen errores tipados;
- Compose traduce;
- no mensajes Firebase;
- cancelación sin UI error.

---

# 10. Fase 9 — Firestore Security Rules

Crear `firestore.rules`:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    match /recipes/{recipeId} {
      allow read: if request.auth != null;

      allow create: if request.auth != null
        && request.resource.data.createdByUid == request.auth.uid;

      allow update: if request.auth != null
        && resource.data.createdByUid == request.auth.uid
        && request.resource.data.createdByUid == resource.data.createdByUid;

      allow delete: if request.auth != null
        && resource.data.createdByUid == request.auth.uid;
    }

    match /users/{email} {
      allow create: if request.auth != null
        && request.auth.token.email == email
        && request.resource.data.uid == request.auth.uid
        && request.resource.data.email == email;

      allow read: if request.auth != null
        && resource.data.uid == request.auth.uid;

      allow update: if request.auth != null
        && resource.data.uid == request.auth.uid
        && request.resource.data.uid == resource.data.uid
        && request.resource.data.email == resource.data.email;

      allow delete: if false;
    }
  }
}
```

Crear/fusionar `firebase.json`:

```json
{
  "firestore": {
    "rules": "firestore.rules"
  }
}
```

Mantener `.whereEqualTo("createdByUid", uid)`.

Responsabilidades:

```text
cliente -> UX
query -> eficiencia
rules -> autorización real
```

Storage Rules fuera de alcance: documentar futura migración `recipes/{uid}/{uuid}.jpg`.

No desplegar automáticamente.

---

# 11. Fase 10 — Testing requerido

Los tests se añaden durante cada paso. Cobertura mínima final:

## 11.1 FakeRecipeRepository

Soportar:

```text
observeRecipes
observeRecipeById
observeRecipesByUser
getRecipeById
createRecipe
updateRecipe
deleteRecipe
uploadRecipeImage -> Result<String>
deleteRecipeImage -> Result<Unit>
```

Configurable para resultados, argumentos y contadores útiles.

## 11.2 RecipeList

- initial Loading;
- Success;
- Error upstream;
- Cancellation no Error;
- re-suscripción si test determinista razonable.

## 11.3 Holder

- campos;
- ingredientes;
- pasos;
- foco;
- imagen;
- populate;
- clear image;
- reset.

## 11.4 Validación

- cada RecipeValidationError;
- RecipeValidationException(reason);
- duration blank;
- duration non-integer;
- duration <=0 domain;
- valid.

## 11.5 Create VM

- invalid no persist;
- no image;
- upload+create success;
- upload fail;
- create fail after upload -> cleanup;
- cleanup fail preserves original;
- cancellation.

## 11.6 Edit VM

- get one-shot;
- not found;
- keep image;
- replace image;
- remove image;
- update fail after upload -> cleanup;
- cleanup fail preserves original;
- cancellation;
- stale load not applied.

## 11.7 Login

Conservar casos actuales, adaptando constructor y errores tipados.

## 11.8 Auth mapper

Todos los casos de 9.9.

## 11.9 Fake vs mock

Mantener fakes. No añadir MockK sin necesidad concreta aprobada.

---

# 12. Fase 11 — CI Android

Crear:

```text
.github/workflows/android-ci.yml
```

Triggers: PR y push master.

Ubuntu + JDK 17 Temurin + setup-gradle.

Ejecutar:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

`google-services.json` no versionado.

Secret:

```text
GOOGLE_SERVICES_JSON_BASE64
```

Comprobarlo, decodificar a `app/google-services.json`, fallar explícitamente si falta.

No publicar a Play.

---

# 13. Fase 12 — README

Actualizar al estado final real.

Documentar:

- Clean Architecture pragmática;
- repositorios directos cuando no hay lógica;
- use cases mantenidos;
- formulario por composición;
- VMs separados;
- validación tipada;
- auth tipado;
- textos resueltos en UI;
- flujo de creación;
- callbackFlow;
- get().await();
- StateFlow + lifecycle;
- Firestore rules;
- ownership cliente vs backend;
- fakes;
- CI;
- deuda Storage Rules.

No documentar features inexistentes.

---

# 14. Restricciones explícitas

NO:

- multi-module;
- KMP/CMP;
- Room/offline-first;
- cambiar Firebase;
- reemplazar Hilt;
- MVI;
- LiveData en lugar de StateFlow;
- replay=1 para eventos;
- eliminar collectAsStateWithLifecycle;
- BaseRecipeViewModel;
- duplicar formulario;
- nuevos NavHost destinations para create/edit;
- mocks masivos;
- Firebase Throwable.message visible;
- textos localizados en domain;
- fallar delete/update confirmado por cleanup ordinario;
- cancelación como error UI;
- cleanup nuevo en AddRecipeViewModel temporal;
- el agente no puede editar, crear, mover, renombrar ni eliminar archivos del proyecto;
- el agente no puede ejecutar por su cuenta los cambios ni los comandos Gradle del proceso guiado;
- deploy sin aprobación;
- cambiar esquema Firestore;
- migrar imágenes antiguas;
- endurecer Storage Rules sin migración.

---

# 15. Definition of Done

- [ ] seis pass-through use cases eliminados;
- [ ] repositorios directos donde procede;
- [ ] cuatro use cases con lógica permanecen;
- [ ] RecipeList sin onStart Loading;
- [ ] get().await() one-shot;
- [ ] no Flow.first() one-shot;
- [ ] upload Result<String>;
- [ ] deleteRecipeImage;
- [ ] delete/update con cleanup best-effort;
- [ ] create/edit cleanup nueva imagen;
- [ ] cleanup nuevo en VMs definitivos;
- [ ] RecipeFormStateHolder;
- [ ] Create/Edit VMs separados;
- [ ] único RecipeFormContent;
- [ ] routes no NavHost destinations;
- [ ] AddRecipeViewModel eliminado;
- [ ] RecipeFormValidator en UI;
- [ ] RecipeValidationError sin textos;
- [ ] RecipeValidationException(reason);
- [ ] form errors tipados;
- [ ] AuthError/AuthException;
- [ ] email marker tipado;
- [ ] USER_DISABLED por errorCode;
- [ ] Login/Register errors tipados;
- [ ] Compose resuelve recursos;
- [ ] no Firebase messages visibles;
- [ ] cancelación nunca error;
- [ ] Firestore Rules versionadas;
- [ ] owner controla escrituras;
- [ ] tests relevantes;
- [ ] CI;
- [ ] README;
- [ ] testDebugUnitTest PASS;
- [ ] lintDebug PASS;
- [ ] assembleDebug PASS.

---

# 16. Cierre del refactor completo

Solo cuando todos los Steps hayan sido implementados manualmente por el desarrollador, revisados y aprobados.

Ejecutar:

```bash
./gradlew testDebugUnitTest lintDebug assembleDebug
```

Responder:

```text
FINAL IMPLEMENTATION REPORT

1. Approved steps completed
- ...

2. Final files added
- ...

3. Final files modified
- ...

4. Final files deleted
- ...

5. Tests added/updated
- ...

6. Final verification
- testDebugUnitTest: PASS/FAIL
- lintDebug: PASS/FAIL
- assembleDebug: PASS/FAIL

7. External/manual actions still required
- Firebase rules deployment
- GitHub secret GOOGLE_SERVICES_JSON_BASE64
- ...

8. Remaining technical debt intentionally out of scope
- ...

9. Deviations from spec approved during the guided process
- None
```

Cualquier problema final abre un nuevo paso y vuelve al protocolo de aprobación.
