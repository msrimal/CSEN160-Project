# How to Build the HeartAttack Project

## Option 1: Using Gradle Wrapper (Recommended)

If you have the wrapper files set up, you can use:

```bash
./gradlew build
```

## Option 2: Install Gradle First

### macOS (using Homebrew):
```bash
brew install gradle
```

### Then build:
```bash
gradle build
```

## Common Gradle Commands

### Build the project:
```bash
./gradlew build
# or
gradle build
```

### Clean and build:
```bash
./gradlew clean build
```

### Run tests:
```bash
./gradlew test
```

### Run the HeartAttack game:
```bash
./gradlew run
# or
./gradlew runHeartAttack
```

### Create a JAR file:
```bash
./gradlew jar
```

### View all available tasks:
```bash
./gradlew tasks
```

## Troubleshooting

If you get "command not found" errors:
1. Make sure you have Java installed: `java -version`
2. Install Gradle OR use the wrapper files
3. Make gradlew executable: `chmod +x gradlew`

