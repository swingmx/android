# Changelog

## [Unreleased]

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