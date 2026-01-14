# Test User Avatars

Avatar images for development test users.

## Files

- `anna-avatar.avif` - Avatar for anna.schmidt@example.com (study_anna)
- `max-avatar.avif` - Avatar for max.meier@example.com (maxlearns)
- `admin-avatar.avif` - Avatar for admin@motivise.app (motadmin)

## Source

Original images from frontend: `webenprj-fe/src/assets/user{1,2,3}.avif`

## Format

Files are in AVIF format (AV1 Image File Format) for optimal file size and quality. The TestDataLoader bypasses the FileUploadValidator, so AVIF format is supported even though it's not in the standard allowed list for user uploads.

## Usage

Loaded by `TestDataLoader` during application startup when:
- Profile: `docker-free` (H2 mode)
- Property: `app.data.load-dev-data=true`

Avatars are uploaded via MediaService and served via `/medias/{uuid}` endpoints.

## How It Works

1. TestDataLoader loads AVIF files from classpath resources
2. Creates MockMultipartFile objects from byte content
3. Uploads via MediaService.upload() (delegates to active FileStorage bean)
4. MediaService creates Media entity with UUID and saves to database
5. Returns Media.id to construct `/medias/{uuid}` URL
6. User.profileImageUrl set to this URL
7. Frontend fetches avatars via MediaController GET /medias/{id} endpoint
