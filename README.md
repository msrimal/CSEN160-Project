# ❤️ HeartAttack Game

A fun, interactive educational game that helps players of all ages learn about the heart while defending it from waves of invading viruses. Answer heart biology quiz questions correctly to survive!

## 🎮 Game Overview

**HeartAttack** is a Java-based tower defense game that combines strategic gameplay with educational elements. Players must use different weapons to eliminate specific virus types while answering heart biology quiz questions that appear at random intervals. Wrong answers cost precious lives, adding an extra layer of challenge to the gameplay.

---

## 🎯 Objective

- **Survive as long as possible** by eliminating viruses before they reach the bottom
- **Match weapons to virus types** - each virus can only be destroyed by its specific counter-weapon
- **Answer quiz questions correctly** to avoid losing lives
- **Progress through increasingly difficult rounds** with more viruses and faster speeds

---

## 🕹️ Controls

### Movement & Combat
- **Arrow Keys** or **A/D**: Move player left/right between lanes
- **Spacebar**: Shoot current weapon
- **1-4 Keys**: Direct weapon selection
  - **1**: Spiky Ball (effective against Spiky viruses)
  - **2**: Ball (effective against Round viruses)  
  - **3**: Star (effective against Star viruses)
  - **4**: Arrow (effective against Arrow viruses)
- **P**: Cycle through weapons (alternative to number keys)

### UI Controls
- **I**: Toggle weapons key visibility on/off
- **R**: Restart game (only available on game over screen)

### Quiz Controls
- **Type letters/numbers**: Input quiz answers
- **Backspace**: Delete characters in quiz input
- **Enter**: Submit quiz answer

---

## 🦠 Virus Types & Weapons

Each virus type has a specific weakness and can **only** be destroyed by its matching weapon:

| Virus Type | Color | Weapon Needed | Key |
|------------|-------|---------------|-----|
| **Spiky Virus** | Red | Spiky Ball | 1 |
| **Round Virus** | Green | Ball | 2 |
| **Star Virus** | Blue | Star | 3 |
| **Arrow Virus** | Orange | Arrow | 4 |

### Combat Mechanics
- **Effective hits**: Using the correct weapon against a virus deals damage
- **Ineffective hits**: Using the wrong weapon has no effect
- **Virus health**: Viruses require multiple hits to destroy
- **Visual feedback**: Viruses become lighter in color as they take damage
- **Strategic positioning**: Move between lanes to target different viruses

---

## 📚 Quiz System

### Timing
- **First quiz**: Appears 35-60 seconds after game starts
- **Subsequent quizzes**: Every 35-60 seconds (random intervals)
- **Game pause**: Gameplay pauses completely during quiz display

### Quiz Mechanics
- **20 questions total** loaded from `questions.json`
- **Current answer**: All questions accept "a" as the correct answer
- **Wrong answers**: Cost 1 life and show "WRONG!" message
- **Correct answers**: Show "CORRECT!" message and continue game
- **Display time**: Results shown for 2 seconds before game resumes

### Quiz Interface
- Large input box with cursor indicator
- Real-time typing display
- Clear instructions and warnings
- Semi-transparent overlay to focus attention

---

## 💖 Lives & Health System

### Lives Display
- **3 lives total** shown as heart icons in top-left corner
- **Red hearts**: Remaining lives
- **Gray hearts**: Lost lives
- **Visual feedback**: Red flash occurs when losing a life

### Ways to Lose Lives
1. **Virus reaches bottom**: Any virus that passes through costs 1 life
2. **Wrong quiz answer**: Incorrect responses cost 1 life
3. **Game over**: Occurs when all 3 lives are lost

---

## 🎵 Audio & Visual Effects

### Sound Effects
- **Round completion**: Single beep when advancing to next round
- **Life loss**: Silent (visual flash only)
- **Game over**: Silent
- **All other actions**: Silent (move, shoot, weapon switch)

### Visual Effects
- **Life loss flash**: Red screen flash when virus passes through
- **Round completion flash**: Green screen flash when round ends
- **Game over screen**: Bright red semi-transparent overlay
- **Virus damage**: Viruses lighten in color as they take damage

---

## 🏆 Round Progression

### Round Structure
- **Round 1**: 3 viruses, slow speed (1.0)
- **Round 2**: 5 viruses, increased speed (1.3)
- **Round 3**: 7 viruses, faster speed (1.6)
- **Round 4+**: Progressively more viruses and higher speeds

### Difficulty Scaling
- **Virus count**: Increases each round (3→5→7→10→13...)
- **Speed**: Viruses move faster in later rounds
- **Spawn rate**: Viruses spawn more frequently as rounds progress
- **Infinite progression**: Game continues until all lives are lost

### Round Completion
- **Condition**: All viruses for the round must be spawned AND eliminated/passed
- **Next round**: Only starts when current round is completely finished
- **Visual cue**: Green flash indicates successful round completion

---

## 🖥️ User Interface

### Main Game UI
- **Lives**: Heart display showing remaining health
- **Round info**: Current round number and progress
- **Virus counter**: Shows remaining viruses in current round
- **Speed indicator**: Current virus movement speed
- **Current weapon**: Displays active weapon type
- **Basic controls**: On-screen control reference

### Weapons Key (Top-right)
- **Toggle**: Press 'I' to show/hide
- **Visual guide**: Shows weapon-virus matchups
- **Color coding**: Current weapon highlighted in cyan
- **Effectiveness guide**: "Only effective against matching virus!"

### Game Over Screen
- **Final round**: Displays the round you reached
- **Restart option**: Press 'R' to play again
- **Consistent restart**: Always begins from Round 1 with 3 viruses

---

## 🛠️ Technical Details

### Requirements
- **Java**: JDK 21+ required
- **Build tool**: Gradle
- **Graphics**: Java Swing with Graphics2D
- **Platform**: Cross-platform (Windows, Mac, Linux)

### Running the Game
```bash
# Navigate to game directory
cd /path/to/HeartAttack

# Run with Gradle
./gradlew run
```

### File Structure
- **Main game**: `src/main/java/com/maya_steph/virusdefense/GamePanel.java`
- **Quiz data**: `questions.json` (20 questions, all answers = "a")
- **Build config**: `build.gradle`

---

## 🎲 Game Tips

### Strategy
1. **Learn the patterns**: Memorize which weapon defeats which virus
2. **Prioritize movement**: Position yourself to hit multiple virus lanes
3. **Quick weapon switching**: Use number keys for fast weapon changes
4. **Quiz preparation**: Remember all answers are "a" for quick responses
5. **Lane management**: Focus on viruses closest to the bottom first

### Weapon Key Memory
- **Red Spiky** → **1** (Spiky Ball)
- **Green Round** → **2** (Ball)  
- **Blue Star** → **3** (Star)
- **Orange Arrow** → **4** (Arrow)

### Quiz Tips
- **Stay calm**: Game pauses during quizzes, so take your time
- **Type "a"**: All current questions have "a" as the correct answer
- **Quick response**: Faster quiz completion means less gameplay interruption

---

## 🏁 Winning & Scoring

### Victory Condition
- **No traditional "win"**: Game continues indefinitely
- **Success metric**: Survive as many rounds as possible
- **Personal best**: Try to beat your highest round number

### Challenge Progression
- **Early rounds**: Learn weapon-virus matchups
- **Mid rounds**: Manage multiple virus types simultaneously  
- **Late rounds**: Handle rapid spawning and high-speed viruses
- **Expert level**: Maintain performance while handling quiz interruptions

---

## 🔧 Customization

### Quiz Questions
- Edit `questions.json` to change questions and answers
- Current format: All answers set to "a" for easy gameplay
- Support for custom questions and varied answers

### Difficulty Adjustments
- Modify quiz timing in code (currently 35-60 seconds)
- Adjust virus speeds and spawn rates
- Change round progression formulas

---

## 🎉 Features Summary

✅ **Strategic tower defense gameplay**  
✅ **4 unique virus types with specific weapon counters**  
✅ **Educational quiz system with life penalties**  
✅ **Progressive difficulty with infinite rounds**  
✅ **Visual and audio feedback systems**  
✅ **Comprehensive UI with toggleable elements**  
✅ **Heart-based life display system**  
✅ **Consistent restart functionality**  
✅ **Real-time damage visualization**  
✅ **Professional game over and round transition effects**

---

*Defend your organs, master the weapons, answer the quizzes, and see how long you can survive in this unique blend of strategy and knowledge!* 🎮🧠