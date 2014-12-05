package com.googlecode.mp4parser;
 
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
 
/**
 * Extracts subtitles.
 */
public class SubtitleExtractionExample {
    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        String pocPath = RemoveSomeSamplesExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/output.mp4";
        Movie poc = MovieCreator.build(pocPath);
        Map<Sample, Long> subtitles = new LinkedHashMap<Sample, Long>();
        for (Track track : poc.getTracks()) {
            if(track.getHandler().equals("sbtl")) {
                List<Sample> subtitleSamples = track.getSamples();
                long[] subtitleDuratons = track.getSampleDurations();
                assert subtitleDuratons.length == subtitleSamples.size();
                for(int i = 0; i<subtitleDuratons.length; i++) {
                    subtitles.put(subtitleSamples.get(i), subtitleDuratons[i]);
                }
            }
        }
 
        if (!subtitles.isEmpty()) {
            Long position = new Long(0);
            for (Sample sample : subtitles.keySet()) {
                ByteBuffer sampleBuffer = sample.asByteBuffer();
                out.print(position + ": ");
                try {
                    short numBytes = sampleBuffer.getShort();
                    byte[] stringBytes = new byte[Short.MAX_VALUE];
                    sampleBuffer.get(stringBytes, 0, numBytes);
                    String string = new String(stringBytes, "UTF-8");
                    out.println(string);
                } catch (IOException e) {
                    throw new RuntimeException("Failed reading sample", e);
                }
                position += subtitles.get(sample);
            }
        }
    }
}
