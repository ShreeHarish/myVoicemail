# myVoicemail
Virtual voicemail app

Work by Shree Harish.S and Srikanth.G.R

Audiorec Package Instructions:

1. Fetch origin, then pull commits into your cloned repository on your computer. Then copy-paste the package in your project.
2. It requires permissions to record audio. So include **"android.permission.RECORD_AUDIO"** in manifest and string[] permissions.
3. To use its methods, import them like this at the top:  
```java
import com.example.audiorec.RecordUtils.RecordButton;
import com.example.audiorec.RecordUtils.PlayButton;
```
4. Then, in your OnCreate function, include this snippet:
```java
String filePath = getExternalCacheDir().getAbsolutePath();
filePath += "/audiorecordtest.3gp";
String logTag = "Eat your food.";

MediaRecorder mr = new MediaRecorder();
MediaPlayer mp = new MediaPlayer();

Button recButton = findViewById(R.id.recButton);
Button playButton = findViewById(R.id.playButton);

RecordButton RB = new RecordButton(recButton, mr, filePath, logTag);
PlayButton PB = new PlayButton(playButton, mp, filePath, logTag);
```
5. That's it. Build and see the magic.
