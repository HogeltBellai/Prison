package ru.hogeltbellai.prison.api.music;

import com.xxmicloxx.NoteBlockAPI.model.RepeatMode;
import com.xxmicloxx.NoteBlockAPI.model.Song;
import com.xxmicloxx.NoteBlockAPI.songplayer.RadioSongPlayer;
import com.xxmicloxx.NoteBlockAPI.songplayer.SongPlayer;
import com.xxmicloxx.NoteBlockAPI.utils.NBSDecoder;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.Prison;

import java.io.File;

public class MusicAPI {

    private String songName;
    private Song song;
    private RadioSongPlayer radioSongPlayer;

    public MusicAPI(String songName) {
        this.songName = songName;

        File musicFolder = new File(Prison.getInstance().getDataFolder(), "music");
        if (!musicFolder.exists()) {
            musicFolder.mkdirs();
        }

        File songFile = new File(musicFolder, songName + ".nbs");
        if (!songFile.exists()) {
            Prison.getInstance().getLogger().warning("Музыка '" + songName + "' не найдена!");
            return;
        }
        song = NBSDecoder.parse(songFile);
        radioSongPlayer = new RadioSongPlayer(song);
        radioSongPlayer.setRepeatMode(RepeatMode.ONE);
        radioSongPlayer.setPlaying(true);
    }

    public void play(Player player) {
        radioSongPlayer.addPlayer(player);
    }

    public void stop(Player player) {
        radioSongPlayer.removePlayer(player);
    }
}
