# Changelog

## [Version 24] - Pre-Release

### Added
- **Hybrid Queue Expansion for Folders**: Intelligent background loading of complete folder contents
  - Music starts playing immediately with visible tracks
  - Remaining tracks load seamlessly in background (50-track chunks)
  - Works with folders containing hundreds or thousands of songs
  - Supports shuffle mode with correct track ordering
  - Smart cancellation when switching between folders

### Improved
- **Folder Navigation**: Enhanced navigation experience
  - Fixed double track number display issues
  - Improved track and folder item overflow handling
  - Better "go to folder" navigation behavior
  - Fixed back navigation after folder actions

### Fixed
- **Media Controller**: Improved queue initialization
  - Fixed media controller initialization when queue is empty
  - Fixed "play next" functionality when queue is empty
  - Better handling of queue state persistence

### Technical
- Added intelligent source comparison for preventing redundant expansions
- Implemented cooperative multitasking for responsive UI during background loading
- Enhanced MediaItem ID mapping for shuffle mode compatibility
- Added proper error handling and cancellation logic for network requests

---

## [Version 23] - 2025-01-02
- Fix Base URL after Log In
- Fix Media Controller Init when Queue is Empty

## [Version 22] - 2025-01-02
- Fix Play Next when Queue is Empty

## [Version 21] - 2025-01-01
- Get Root Dirs and Fix Back Nav on Folders
- Fix Back Press after Go to Folder Action

## [Version 20] - 2024-12-31
- Use Index as Track Keys