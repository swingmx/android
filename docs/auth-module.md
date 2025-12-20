# Auth Module

Self-contained authentication feature module using MVVM + Repository pattern.

## Structure

```
auth/
├── data/
│   ├── api/           # Retrofit API service
│   ├── datastore/     # Token persistence (DataStore)
│   ├── di/            # Hilt modules
│   ├── dto/           # API response models
│   ├── repository/    # AuthRepository implementation
│   ├── tokenholder/   # In-memory token storage
│   └── workmanager/   # Background token refresh
├── domain/
│   ├── model/         # Domain models
│   └── repository/    # Repository interface
└── presentation/
    ├── screen/        # Login screens (QR, Username)
    ├── state/         # UI state classes
    └── viewmodel/     # AuthViewModel
```

## API Endpoints

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/auth/login` | POST | Username/password login |
| `/auth/pair?code=` | GET | QR code pairing |
| `/auth/refresh` | POST | Token refresh |
| `/auth/users` | GET | Get all users |
| `/auth/profile/create` | POST | Create user |

## Authentication Flows

### Username/Password
1. User enters server URL + credentials
2. ViewModel validates input, calls API
3. Tokens stored in DataStore + memory
4. Navigate to home

### QR Code
1. Scan QR from web client (format: `URL CODE`)
2. API call to `/auth/pair`
3. Same token storage and navigation

### Token Refresh
- `TokenRefreshWorker` runs every 6 hours via WorkManager
- Automatically refreshes tokens in background

## Key Classes

| Class | Location | Responsibility |
|-------|----------|----------------|
| `AuthApiService` | `data/api/` | Retrofit API calls |
| `AuthTokensDataStore` | `data/datastore/` | Persistent token storage |
| `AuthTokenHolder` | `data/tokenholder/` | In-memory token access |
| `DataAuthRepository` | `data/repository/` | Coordinates API + storage |
| `AuthViewModel` | `presentation/viewmodel/` | UI state management |
| `LoginWithQrCode` | `presentation/screen/` | QR scanner screen |
| `LoginWithUsername` | `presentation/screen/` | Form login screen |

## State Management

```kotlin
data class AuthUiState(
    val baseUrl: String?,
    val username: String?,
    val password: String?,
    val authState: AuthState,  // LOGGED_OUT | AUTHENTICATED
    val isLoading: Boolean,
    val authError: AuthError   // None | InputError | LoginError
)
```

## Dependencies

- `:database` - User entity, DAOs
- `:core` - Resource wrapper
- `:uicomponent` - UI components

## Error Codes

- `401` - Incorrect password
- `404` - User not found
