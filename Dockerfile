# Build APK for Vote4TechVotacionUrna
FROM mingc/android-build-box:latest

WORKDIR /app
COPY . .

# Set Gradle permissions
RUN chmod +x gradlew

# Build release APK
RUN ./gradlew assembleRelease --no-daemon

# Output APK at /app/app/build/outputs/apk/release/app-release-unsigned.apk
