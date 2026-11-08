# Adding the design fonts (optional)

The app currently uses system fonts so it builds without any font files.
To use the Stitch design fonts (Space Grotesk, Inter, JetBrains Mono):

1. Download each family from fonts.google.com.
2. Open the **static** folder inside each download.
3. Copy these files into `app/src/main/res/font/` and rename them exactly:

| Copy from the static folder | Rename to |
|---|---|
| SpaceGrotesk-SemiBold.ttf | space_grotesk_semibold.ttf |
| SpaceGrotesk-Bold.ttf | space_grotesk_bold.ttf |
| Inter-Regular.ttf | inter_regular.ttf |
| Inter-Medium.ttf | inter_medium.ttf |
| Inter-SemiBold.ttf | inter_semibold.ttf |
| JetBrainsMono-Regular.ttf | jetbrains_mono_regular.ttf |
| JetBrainsMono-Medium.ttf | jetbrains_mono_medium.ttf |
| JetBrainsMono-SemiBold.ttf | jetbrains_mono_semibold.ttf |

Android requires lowercase file names. Do not put README or other text files
in `res/font/`, because the build will reject them.

4. In `app/src/main/java/com/gamecontrol/app/ui/theme/Type.kt`, replace the three
   `FontFamily.*` values with `FontFamily(Font(R.font.<name>, <weight>), ...)` definitions.
