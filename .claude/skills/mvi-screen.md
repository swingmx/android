---
description: Build swingmx screens using the standard MVI pattern. Screen + ScreenContent + ViewModel + UiState + UiEvent + UiEffect. Initial fetches go in VM init {}. One-shot effects flow through a Channel and are collected with ObserverAsEvent.
---

# MVI screen pattern

Use this for any new screen, or when refactoring an existing screen.

## File layout

For a screen called `Foo` in feature module `feature/<area>`:

```
feature/<area>/src/main/java/com/android/swingmusic/<area>/presentation/
  event/FooUiEvent.kt
  event/FooUiEffect.kt   (or keep effects in their own folder if you prefer)
  state/FooUiState.kt
  screen/FooScreen.kt
  viewmodel/FooViewModel.kt
```

## UiState

`@Immutable data class`. Per-resource fields: `isLoadingX`, `errorLoadingX`, the data. Sheet / dialog visibility flags. `isRefreshing: Boolean`.

```kotlin
@Immutable
internal data class FooUiState(
    val isRefreshing: Boolean = false,

    val isLoadingResource1: Boolean = false,
    val errorLoadingResource1: String? = null,
    val resource1: Resource1? = null,

    val showSomethingSheet: Boolean = false,
)
```

## UiEvent

`sealed interface`. One object / data class per user action.

```kotlin
internal sealed interface FooUiEvent {
    data object OnBackPressed : FooUiEvent
    data object OnRefresh : FooUiEvent
    data class OnItemClicked(val id: Int) : FooUiEvent
}
```

## UiEffect

`sealed class`. One-shot effects only (navigation, snackbar, toast, vibration, etc.). Never put state-shaped data here, that belongs in `UiState`.

```kotlin
internal sealed class FooUiEffect {
    data class ShowSnackBar(val message: String) : FooUiEffect()
    data class ShowToast(val message: String) : FooUiEffect()
    data object NavigateBack : FooUiEffect()
    data class NavigateToBar(val id: Int) : FooUiEffect()
}
```

## ViewModel

Rules:

- `MutableStateFlow<UiState>` exposed as `asStateFlow()`.
- `Channel<UiEffect>(Channel.UNLIMITED)` exposed as `receiveAsFlow()`.
- `SavedStateHandle` for route args.
- **`init { }` runs the initial fetches.** Not a `LaunchedEffect` in the screen.
- Single `onEvent(event)` entry point with a `when` block.
- Public `refresh()` for pull-to-refresh, which re-runs the same fetches with `isRefreshing = true`.
- Private `updateUiState { copy(...) }` helper to keep call sites short.

```kotlin
@HiltViewModel
internal class FooViewModel @Inject constructor(
    private val getResource1UseCase: GetResource1UseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val idFromNav: Int = savedStateHandle.get<Int>("id") ?: -1

    private val _uiState = MutableStateFlow(FooUiState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = Channel<FooUiEffect>(capacity = Channel.UNLIMITED)
    val uiEffect = _uiEffect.receiveAsFlow()

    init {
        getResource1()
    }

    fun onEvent(event: FooUiEvent) {
        when (event) {
            FooUiEvent.OnBackPressed -> _uiEffect.trySend(FooUiEffect.NavigateBack)
            FooUiEvent.OnRefresh -> refresh()
            is FooUiEvent.OnItemClicked -> _uiEffect.trySend(FooUiEffect.NavigateToBar(event.id))
        }
    }

    fun refresh() {
        getResource1(isRefreshing = true)
    }

    private fun getResource1(isRefreshing: Boolean = false) {
        updateUiState {
            copy(
                isRefreshing = isRefreshing,
                isLoadingResource1 = !isRefreshing,
                errorLoadingResource1 = null,
            )
        }
        viewModelScope.launch {
            // call use case, update state with result
        }
    }

    private fun updateUiState(block: FooUiState.() -> FooUiState) {
        _uiState.update(block)
    }
}
```

## Screen

- Inject VM via `hiltViewModel()`.
- Collect `uiState` with `collectAsState()`.
- Collect `uiEffect` with `ObserverAsEvent(viewModel.uiEffect) { ... }`. Never raw `LaunchedEffect { uiEffect.collect { } }`.
- Delegate UI to a stateless `ScreenContent`.
- **No `LaunchedEffect` for initial data fetches.** The VM `init { }` handles it.

```kotlin
@Destination
@Composable
internal fun FooScreen(
    navigator: FooNavigator,
    modifier: Modifier = Modifier,
    viewModel: FooViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    ObserverAsEvent(viewModel.uiEffect) { effect ->
        when (effect) {
            is FooUiEffect.ShowSnackBar -> scope.launch {
                snackbarHostState.showSnackbar(effect.message)
            }
            is FooUiEffect.ShowToast -> Toast.makeText(
                context, effect.message, Toast.LENGTH_SHORT
            ).show()
            FooUiEffect.NavigateBack -> navigator.popBackStack()
            is FooUiEffect.NavigateToBar -> navigator.goToBar(effect.id)
        }
    }

    FooScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    )
}

@Composable
private fun FooScreenContent(
    uiState: FooUiState,
    onEvent: (FooUiEvent) -> Unit,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // stateless UI here. previewable.
}

@Preview
@Composable
private fun FooScreenContentPreview() {
    SwingMusicTheme {
        FooScreenContent(
            uiState = FooUiState(/* sample state */),
            onEvent = {},
            snackbarHost = {},
        )
    }
}
```

## ObserverAsEvent

Lives at `uicomponent/src/main/java/com/android/swingmusic/uicomponent/presentation/util/ObserverAsEvent.kt`. Always use this for one-shot effect collection. It ties collection to the STARTED lifecycle state (so effects don't fire while the screen is hidden) and uses `Dispatchers.Main.immediate` so navigation feels synchronous.

```kotlin
import com.android.swingmusic.uicomponent.presentation.util.ObserverAsEvent
```

## Don'ts

- No `LaunchedEffect` in the Screen for initial data fetches. Use VM `init { }`.
- No `loadedInitialData` flag. The VM lifecycle handles it.
- No raw `LaunchedEffect { uiEffect.collect { } }`. Use `ObserverAsEvent`.
- No `@Preview` on the `Screen` composable (it has a real VM). Preview only `ScreenContent`.
- Don't reach into Repository directly from the VM if a UseCase exists. Go through the UseCase.
- Don't put state-shaped data in `UiEffect`. Only one-shot effects belong there.
