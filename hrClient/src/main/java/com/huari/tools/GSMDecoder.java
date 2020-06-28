package com.huari.tools;

import com.huari.Base.AnalysisBase;

import java.io.*;
import java.lang.*;

public class GSMDecoder {
    public static final short MIN_WORD  = -32768;
    public static final short MAX_WORD  = 32767;

    /*   Table 4.6   Normalized direct mantissa used to compute xM/xmax     */
    /* i               0      1      2      3      4      5      6      7   */
    public static final short gsm_FAC[] =
            { 18431, 20479, 22527, 24575, 26623, 28671, 30719, 32767 };

    /*   Table 4.3b   Quantization levels of the LTP gain quantizer */
    /* bc                                    0      1      2      3 */
    public static final short gsm_QLB[] = { 3277, 11469, 21299, 32767 };

    /* chunk_size is the number of bytes to read before decoding */
    private static final int MAX_FRAME_READ = 100;
    public static final short FRAME_SIZE = 33;
    private static int GSM_FRAME_SIZE = 33;
    private static final int chunk_size = MAX_FRAME_READ * FRAME_SIZE;

    private int gsm_left = 0;
    private byte[] gsm_left_bytes;

    public int temp_audiolength = 0;
    public byte[] temp_audiodata;

    /* Every Decoder has a state through completion */
    private short[] dp0;
    private short[] u; 		/* short_term          */
    private short[][] LARpp; 	/*                     */
    private short j;  		/*                     */
    private short nrp; 		/* synthesis  	       */
    private short[] v; 		/* synthesis 	       */
    private short msr; 		/* Postprocessing      */

    public boolean WAV49 = false;

    private boolean frame_index = false;
    private char frame_chain;

    private static int bytes_sent = 0;

    public GSMDecoder(boolean pWAV49)
    {
        dp0 = new short[280];
        u = new short[8];
        LARpp = new short[2][8];
        nrp = 40;
        v = new short[9];

        WAV49 = pWAV49;

        gsm_left_bytes = new byte[160];

        temp_audiodata = new byte[640];
    }

    public void process_frame(InputStream from) throws Exception
    {
        GSM_FRAME_SIZE = 33;
        bytes_sent = 0;

        byte[] frame_chunk = new byte[chunk_size];
        int index = 0;
        int bytes_read = 0;
        int total_bytes = 0;

        if (gsm_left>0)
            System.arraycopy(gsm_left_bytes,0,frame_chunk,0,gsm_left);

        while (bytes_read != -1) { // Read till end of inputstream.

            for(index = 0;
                index < MAX_FRAME_READ && bytes_read != -1;
                ++index)
            {
                bytes_read = get_frame(from, frame_chunk, total_bytes);

                if (bytes_read != -1) {
                    total_bytes += bytes_read;
                }
            }

            // Decode the entire frame chunk.
            if (bytes_read != -1) {
                process_decode(MAX_FRAME_READ, frame_chunk);
            }
            // Decode only part of the frame chunk.
            else if (bytes_read == -1 && total_bytes < chunk_size) {
                int partial_frame = total_bytes / FRAME_SIZE+1;
                process_decode(partial_frame, frame_chunk);

//                gsm_left = total_bytes % FRAME_SIZE;
//                if (gsm_left > 0)
//                    System.arraycopy(frame_chunk,partial_frame*65, gsm_left_bytes,0, gsm_left);
            }

            total_bytes = 0; // Get ready for next frame chunk. If there is one.
        } // end while.

        //    System.out.println("process_frame, bytes_sent: " + bytes_sent);
        //    System.out.println("process_frame, frames_sent: " + frames_sent);

    }

    private int get_frame (InputStream in, byte[] chunk, int start_at)
            throws Exception {
        int read_size = FRAME_SIZE;
        int bytes_read = 0;
        int rc;

        if (WAV49) {
            read_size = GSM_FRAME_SIZE;
            // for the next time
            if (GSM_FRAME_SIZE == 33)
                GSM_FRAME_SIZE = 32;
            else
                GSM_FRAME_SIZE = 33;

            if ( gsm_left > 0 )
                read_size -= gsm_left;
        }

        try {
            rc = in.read( chunk, start_at + bytes_read, read_size);

            while( rc > 0 && (rc < read_size) ) {
                bytes_read = bytes_read + rc;
                read_size -= rc;
                rc = in.read( chunk, start_at + bytes_read, read_size);
            }
        } catch (IOException e) {
            throw new Exception( e.getMessage());
        }

        bytes_sent += rc+bytes_read;

        if (bytes_sent >= 65) {
            //frames_sent++;
            bytes_sent -= 65;
        }

        //    System.out.println("get_frame, bytes_sent: " + bytes_sent);

        //    System.out.println("get_frame, returning: " + (rc < 0 ? rc : rc+bytes_read ));
        return( rc < 0 ? rc : rc+bytes_read );
    }


    private void process_decode(int num_frames, byte[] chunk)
    {
        int frame_size = FRAME_SIZE;
        byte[] gsm_frame = new byte[FRAME_SIZE];

        int index;
        int chunk_index = 0;

        temp_audiolength = num_frames*160;
        temp_audiodata = new byte[temp_audiolength];

        for (index = 0; index < num_frames; ++index) {
            // Get a single gsm frame.
            System.arraycopy(chunk, chunk_index, gsm_frame, 0, frame_size);

            if (WAV49) {
                // setup for next frame
                if (frame_size == 33) {
                    chunk_index += 33;
                    frame_size = 32;
                } else {
                    chunk_index += 32;
                    frame_size = 33;
                }
            } else
                chunk_index += 33;

            // Decode the gsm_frame and produce the output_signal.
            int[] output_signal = gsm_decode_java(gsm_frame);

            ulaw_output(output_signal);
            // Write the output_signal to the stream.
            //System.arraycopy(ulaw_output(output_signal),0,temp_audiodata,index*160,160);
        }
    }


    //private byte[] ulaw_output( int[] signal)
    private void ulaw_output( int[] signal)
    {
        byte[] temp = new byte[160];

        for(int i = 0; i < 160; i++) {
            temp[i] = (byte) S2U.s2u[((0xFFFF & signal[i]) >> 3)];
        }
        Thread thread = new Thread(() -> {
            try {
                AnalysisBase.at.write(temp, 0, 160);
                Thread.sleep(5);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        thread.start();

        //return temp;
    }

    private static String getBits( byte value ) {
        int displayMask = 1 << 7;
        StringBuffer buf = new StringBuffer( 10 );

        for ( int c = 1; c <= 8; c++ ) {
            buf.append(( value & displayMask ) == 0 ? '0' : '1' );
            value <<= 1;

            if ( c % 8 == 0 )
                buf.append( ' ' );
        }
        return buf.toString();
    }

    private static String getBits( short value ) {
        int displayMask = 1 << 15;
        StringBuffer buf = new StringBuffer( 20 );

        for ( int c = 1; c <= 16; c++ ) {
            buf.append(( value & displayMask ) == 0 ? '0' : '1' );
            value <<= 1;

            if ( c % 8 == 0 )
                buf.append( ' ' );
        }
        return buf.toString();
    }


    private int[] gsm_decode_java( byte[] frame )
    {
        short LARc[] =  new short[8];     /* [0..7]      IN      */
        short Nc[] =    new short[4];     /* [0..3]      IN      */
        short Mc[] =    new short[4];     /* [0..3]      IN      */
        short bc[] =    new short[4];     /* [0..3]      IN      */
        short xmaxc[] = new short[4];     /* [0..3]      IN      */
        short xmc[] =   new short[13*4];  /* [0..13*4]   IN      */

        int i = 0;
        if (WAV49) {

            short sr = 0;

            frame_index = !frame_index;

            //      System.out.println ("In WAV49 loop: " + frame_index);
            if (frame_index) {

                sr = (short) (frame[i++] & 0xFF);
                LARc[0] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                sr |= (frame[i++] << 2) & 0x3FF;
                LARc[1] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                sr |= (frame[i++] << 4) & 0x0FFF;
                LARc[2] = (short) (sr & 0x1f);  sr = (short) ((0xFFFF & sr) >>> 5);
                //        System.out.println("sr: " + sr + " " + getBits (sr));
                LARc[3] = (short) (sr & 0x1f);  sr = (short) ((0xFFFF & sr) >>> 5);
                sr |= (frame[i++] << 2) & 0x3FF;
                LARc[4] = (short) (sr & 0xf);  sr = (short) ((0xFFFF & sr) >>> 4);
                LARc[5] = (short) (sr & 0xf);  sr = (short) ((0xFFFF & sr) >>> 4);
                sr |= (frame[i++] << 2) & 0x3FF;			/* 5 */
                LARc[6] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                LARc[7] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 4) & 0x0FFF;
                Nc[0] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                bc[0] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[0] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmaxc[0] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[0] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[1] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[2] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[3] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[4] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[5] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;			/* 10 */
                xmc[6] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[7] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[8] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[9] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[10] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[11] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[12] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 4) & 0x0FFF;
                Nc[1] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                bc[1] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[1] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmaxc[1] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[13] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);				/* 15 */
                xmc[14] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[15] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[16] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[17] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[18] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[19] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[20] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[21] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[22] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[23] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[24] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[25] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 4) & 0x0FFF;			/* 20 */
                Nc[2] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                bc[2] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[2] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmaxc[2] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[26] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[27] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[28] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[29] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[30] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[31] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[32] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[33] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[34] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);				/* 25 */
                xmc[35] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[36] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[37] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[38] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 4) & 0x0FFF;
                Nc[3] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                bc[3] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[3] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmaxc[3] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[39] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[40] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[41] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;			/* 30 */
                xmc[42] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[43] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[44] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[45] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[46] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[47] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[48] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[49] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[50] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[51] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);

                frame_chain = (char) (sr & 0xf);

            } else {

                sr = (short) frame_chain;
                sr |= (frame[i++] << 4) & 0x0FFF;			/* 1 */
                LARc[0] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                LARc[1] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                sr = (short) (frame[i++] & 0xFF);
                LARc[2] = (short) (sr & 0x1f);  sr = (short) ((0xFFFF & sr) >>> 5);
                sr |= (frame[i++] << 3) & 0x07FF;
                LARc[3] = (short) (sr & 0x1f);  sr = (short) ((0xFFFF & sr) >>> 5);
                LARc[4] = (short) (sr & 0xf);  sr = (short) ((0xFFFF & sr) >>> 4);
                sr |= (frame[i++] << 2) & 0x3FF;
                LARc[5] = (short) (sr & 0xf);  sr = (short) ((0xFFFF & sr) >>> 4);
                LARc[6] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                LARc[7] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);				/* 5 */
                Nc[0] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                sr |= (frame[i++] << 1) & 0x01FF;
                bc[0] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[0] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 5) & 0x1FFF;
                xmaxc[0] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[0] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[1] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[2] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[3] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[4] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[5] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[6] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;			/* 10 */
                xmc[7] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[8] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[9] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[10] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[11] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[12] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                Nc[1] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                sr |= (frame[i++] << 1) & 0x01FF;
                bc[1] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[1] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 5) & 0x1FFF;
                xmaxc[1] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[13] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[14] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;			/* 15 */
                xmc[15] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[16] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[17] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[18] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[19] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[20] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[21] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[22] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[23] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[24] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[25] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                Nc[2] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                sr |= (frame[i++] << 1) & 0x01FF;			/* 20 */
                bc[2] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[2] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 5) & 0x1FFF;
                xmaxc[2] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[26] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[27] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[28] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[29] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[30] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                xmc[31] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[32] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[33] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[34] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[35] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;			/* 25 */
                xmc[36] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[37] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[38] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);
                Nc[3] = (short) (sr & 0x7f);  sr = (short) ((0xFFFF & sr) >>> 7);
                sr |= (frame[i++] << 1) & 0x01FF;
                bc[3] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                Mc[3] = (short) (sr & 0x3);  sr = (short) ((0xFFFF & sr) >>> 2);
                sr |= (frame[i++] << 5) & 0x1FFF;
                xmaxc[3] = (short) (sr & 0x3f);  sr = (short) ((0xFFFF & sr) >>> 6);
                xmc[39] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[40] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[41] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[42] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[43] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr = (short) (frame[i++] & 0xFF);				/* 30 */
                xmc[44] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[45] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 2) & 0x3FF;
                xmc[46] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[47] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[48] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                sr |= (frame[i++] << 1) & 0x01FF;
                xmc[49] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[50] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);
                xmc[51] = (short) (sr & 0x7);  sr = (short) ((0xFFFF & sr) >>> 3);

            }

        } else {

            LARc[0]  = (short) ((frame[i++] & 0xF) << 2);           /* 1 */
            LARc[0] |= (frame[i] >> 6) & 0x3;
            LARc[1]  = (short) (frame[i++] & 0x3F);
            LARc[2]  = (short) ((frame[i] >> 3) & 0x1F);
            //      System.out.println ("frame[i]: " + frame[i] + " " + getBits(frame[i]));
            LARc[3]  = (short) ((frame[i++] & 0x7) << 2);
            //      System.out.println ("frame[i]: " + frame[i] + " " + getBits(frame[i]));
            LARc[3] |= (frame[i] >> 6) & 0x3;
            LARc[4]  = (short) ((frame[i] >> 2) & 0xF);
            LARc[5]  = (short) ((frame[i++] & 0x3) << 2);
            LARc[5] |= (frame[i] >> 6) & 0x3;
            LARc[6]  = (short) ((frame[i] >> 3) & 0x7);
            LARc[7]  = (short) (frame[i++] & 0x7);
            Nc[0]  = (short) ((frame[i] >> 1) & 0x7F);
            bc[0]  = (short) ((frame[i++] & 0x1) << 1);
            bc[0] |= (frame[i] >> 7) & 0x1;
            Mc[0]  = (short) ((frame[i] >> 5) & 0x3);
            xmaxc[0]  = (short) ((frame[i++] & 0x1F) << 1);
            xmaxc[0] |= (frame[i] >> 7) & 0x1;
            xmc[0]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[1]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[2]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[2] |= (frame[i] >> 6) & 0x3;
            xmc[3]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[4]  = (short) (frame[i++] & 0x7);
            xmc[5]  = (short) ((frame[i] >> 5) & 0x7);
            xmc[6]  = (short) ((frame[i] >> 2) & 0x7);
            xmc[7]  = (short) ((frame[i++] & 0x3) << 1);            /* 10 */
            xmc[7] |= (frame[i] >> 7) & 0x1;
            xmc[8]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[9]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[10]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[10] |= (frame[i] >> 6) & 0x3;
            xmc[11]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[12]  = (short) (frame[i++] & 0x7);
            Nc[1]  = (short) ((frame[i] >> 1) & 0x7F);
            bc[1]  = (short) ((frame[i++] & 0x1) << 1);
            bc[1] |= (frame[i] >> 7) & 0x1;
            Mc[1]  = (short) ((frame[i] >> 5) & 0x3);
            xmaxc[1]  = (short) ((frame[i++] & 0x1F) << 1);
            xmaxc[1] |= (frame[i] >> 7) & 0x1;
            xmc[13]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[14]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[15]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[15] |= (frame[i] >> 6) & 0x3;
            xmc[16]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[17]  = (short) (frame[i++] & 0x7);
            xmc[18]  = (short) ((frame[i] >> 5) & 0x7);
            xmc[19]  = (short) ((frame[i] >> 2) & 0x7);
            xmc[20]  = (short) ((frame[i++] & 0x3) << 1);
            xmc[20] |= (frame[i] >> 7) & 0x1;
            xmc[21]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[22]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[23]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[23] |= (frame[i] >> 6) & 0x3;
            xmc[24]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[25]  = (short) (frame[i++] & 0x7);
            Nc[2]  = (short) ((frame[i] >> 1) & 0x7F);
            bc[2]  = (short) ((frame[i++] & 0x1) << 1);             /* 20 */
            bc[2] |= (frame[i] >> 7) & 0x1;
            Mc[2]  = (short) ((frame[i] >> 5) & 0x3);
            xmaxc[2]  = (short) ((frame[i++] & 0x1F) << 1);
            xmaxc[2] |= (frame[i] >> 7) & 0x1;
            xmc[26]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[27]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[28]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[28] |= (frame[i] >> 6) & 0x3;
            xmc[29]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[30]  = (short) (frame[i++] & 0x7);
            xmc[31]  = (short) ((frame[i] >> 5) & 0x7);
            xmc[32]  = (short) ((frame[i] >> 2) & 0x7);
            xmc[33]  = (short) ((frame[i++] & 0x3) << 1);
            xmc[33] |= (frame[i] >> 7) & 0x1;
            xmc[34]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[35]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[36]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[36] |= (frame[i] >> 6) & 0x3;
            xmc[37]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[38]  = (short) (frame[i++] & 0x7);
            Nc[3]  = (short) ((frame[i] >> 1) & 0x7F);
            bc[3]  = (short) ((frame[i++] & 0x1) << 1);
            bc[3] |= (frame[i] >> 7) & 0x1;
            Mc[3]  = (short) ((frame[i] >> 5) & 0x3);
            xmaxc[3]  = (short) ((frame[i++] & 0x1F) << 1);
            xmaxc[3] |= (frame[i] >> 7) & 0x1;
            xmc[39]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[40]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[41]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[41] |= (frame[i] >> 6) & 0x3;
            xmc[42]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[43]  = (short) (frame[i++] & 0x7);                  /* 30  */
            xmc[44]  = (short) ((frame[i] >> 5) & 0x7);
            xmc[45]  = (short) ((frame[i] >> 2) & 0x7);
            xmc[46]  = (short) ((frame[i++] & 0x3) << 1);
            xmc[46] |= (frame[i] >> 7) & 0x1;
            xmc[47]  = (short) ((frame[i] >> 4) & 0x7);
            xmc[48]  = (short) ((frame[i] >> 1) & 0x7);
            xmc[49]  = (short) ((frame[i++] & 0x1) << 2);
            xmc[49] |= (frame[i] >> 6) & 0x3;
            xmc[50]  = (short) ((frame[i] >> 3) & 0x7);
            xmc[51]  = (short) (frame[i] & 0x7);                    /* 33 */

        }

        //    System.out.println ("LARc, Nc: " + getArrayString (LARc) + " | " + getArrayString (Nc) );

        return (Gsm_Decoder_java(LARc, Nc, Mc, bc, xmaxc, xmc));
    }

    private String getArrayString (short [] pShorts) {
        String x = "";
        for (int i = 0; i < pShorts.length; i++)
            x += pShorts [i] + " ";
        return x;
    }

    private int[] Gsm_Decoder_java(short LARc[], short Nc[], short Mc[],
                                   short bc[], short xmaxc[], short xmc[])
    {
        int xmc_start = 0;
        short[] wt  = new short[160];
        short[] erp = new short[40];     /* [0..39]                IN  */

        int drp_start_dp0 = 120;         /* Index dp0[280] */

        /* xmc_start += 13;  13 looped 4 times = 13*4 or xmc[13*4] */
        for (int j = 0; j <= 3; j++, xmc_start += 13) {
            Gsm_RPE_Decoding_java( xmaxc[j], Mc[j], xmc_start, xmc, erp );
            Gsm_Long_Term_Synthesis_Filtering( Nc[j], bc[j], erp, drp_start_dp0 );
            System.arraycopy( dp0, drp_start_dp0, wt, (j * 40), 39 );
        }
        int[] out = Gsm_Short_Term_Synthesis_Filter( LARc, wt );

        Postprocessing( out );

        return( out );
    }

    private void Gsm_RPE_Decoding_java
            (short xmaxc_elem,  /* From Gsm_Decoder xmaxc short array */
             short Mc_elem,     /* From Gsm_Decoder Mc short array */
             int xmc_start,     /* Starting point for the three bit part of xmc */
             short[] xmc,       /* [0..12], 3 bits     IN      */
             short[] erp        /* [0..39]             OUT     */  )
    {
        short    xMp[] = new short[13];
        short    expAndMant[];

        /* exp_out and mant_out are modified in this method */
        expAndMant = APCM_quantization_xmaxc_to_exp_mant( xmaxc_elem );
        APCM_inverse_quantization(xmc, xMp, xmc_start, expAndMant[0], expAndMant[1]);
        RPE_grid_positioning(Mc_elem, xMp, erp);
    }

    private short[] APCM_quantization_xmaxc_to_exp_mant( short xmaxc_elem )
            throws IllegalArgumentException {
        short    exp = 0, mant = 0;
        /* Compute exponent and mantissa of the decoded version of xmaxc
         */
        if (xmaxc_elem > 15) {
            exp = (short) (SASR(xmaxc_elem, 3) - 1);
        }
        mant = (short) (xmaxc_elem - (exp << 3));

        if (mant == 0) {
            exp = (short) -4;
            mant = (short) 7;
        }
        else {
            while (mant <= 7) {
                mant = (short) (mant << 1 | 1);
                exp--;
            }
            mant -= (short) 8;
        }
        if( exp < -4 || exp > 6 ) {
            throw new IllegalArgumentException
                    ("APCM_quantization_xmaxc_to_exp_mant: exp = "
                            +exp+" is out of range. Should be >= -4 and <= 6");
        }
        if( mant < 0 || mant > 7 ) {
            throw new IllegalArgumentException
                    ("APCM_quantization_xmaxc_to_exp_mant: mant = "
                            +mant+" is out of range. Should be >= 0 and <= 7");
        }
        short[] exp_mant = new short[2];
        exp_mant[0] = exp;
        exp_mant[1] = mant;
        return exp_mant;
    }

    /*
     *  This part is for decoding the RPE sequence of coded xMc[0..12]
     *  samples to obtain the xMp[0..12] array.  Table 4.6 is used to get
     *  the mantissa of xmaxc (FAC[0..7]).
     */
    private void APCM_inverse_quantization(
            short[] xmc,   /* [0..12] IN  */
            short[] xMp,   /* [0..12] OUT */
            int     xmc_start,
            short	exp_out,
            short	mant_out)
            throws IllegalArgumentException {

        short      temp, temp1, temp2, temp3;
        temp1 = gsm_FAC[ mant_out ];
        temp2 = GSM_SUB( (short)6, exp_out );
        temp3 = gsm_asl( (short)1, GSM_SUB( temp2, (short)1 ));

        int xMp_point = 0;
        for (int i = 0; i < 13; i++) {
            /* restore sign   */
            temp = (short) ((xmc[xmc_start++] << 1) - 7);
            if( ! (temp <= 7 && temp >= -7) ) {    /* 4 bit signed   */
                throw new IllegalArgumentException
                        ("APCM_inverse_quantization: temp = "
                                +temp+" is out of range. Should be >= -7 and <= 7");
            }
            temp <<= 12;                        /* 16 bit signed  */
            temp = GSM_MULT_R( temp1, temp );
            temp = GSM_ADD( temp, temp3 );
            xMp[ xMp_point++ ] = gsm_asr( temp, temp2 );
        }
    }

    /*
     *  This method computes the reconstructed long term residual signal
     *  ep[0..39] for the LTP analysis filter.  The inputs are the Mc
     *  which is the grid position selection and the xMp[0..12] decoded
     *  RPE samples which are upsampled by a factor of 3 by inserting zero
     *  values.
     */
    private static void RPE_grid_positioning(
            short   Mc,            /* grid position        IN      */
            short[] xMp,           /* [0..12]              IN      */
            short[] ep)            /* [0..39]              OUT     */
            throws IllegalArgumentException {

        int     i = 13;
        int     xMp_index = 0;
        int     ep_index = 0;

        if( ! (0 <= Mc && Mc <= 3) ) {
            throw new IllegalArgumentException
                    ("RPE_grid_positioning: Mc = "
                            +Mc+" is out of range. Should be >= 0 and <= 3");
        }
        switch(Mc) {
            case 3: ep[ep_index++] = 0;
            case 2: ep[ep_index++] = 0;
            case 1: ep[ep_index++] = 0;
            case 0: ep[ep_index++] = xMp[xMp_index++];
                i--;
        };
        do {
            ep[ep_index++] = 0;
            ep[ep_index++] = 0;
            ep[ep_index++] = xMp[xMp_index++];
        } while (--i>0);

        while (++Mc < 4) {
            ep[ep_index++] = 0;
        }
    }

    /*
     *  This procedure uses the bcr and Ncr parameter to realize the
     *  long term synthesis filtering.  The decoding of bcr needs
     *  table 4.3b.
     */
    private void Gsm_Long_Term_Synthesis_Filtering(
            short          Ncr,
            short          bcr,
            short[]        erp,          /* [0..39]                IN  */
            int dp0_index_start_drp      /* [-120..-1] IN, [0..40] OUT */
            /* drp is a pointer into the Gsm_State
             * dp0 short array. */
    )
            throws IllegalArgumentException {

        short     brp, drpp, Nr;
        short[]   drp = dp0;
        /*  Check the limits of Nr.
         */
        Nr = Ncr < 40 || Ncr > 120 ? nrp : Ncr;
        nrp = Nr;
        if( ! (Nr >= 40 && Nr <= 120) ) {
            throw new IllegalArgumentException
                    ("Gsm_Long_Term_Synthesis_Filtering Nr = "
                            +Nr+" is out of range. Should be >= 40 and <= 120");
        }
        /*  Decoding of the LTP gain bcr
         */
        brp = gsm_QLB[ bcr ];
        /*  Computation of the reconstructed short term residual
         *  signal drp[0..39]
         */
        if(brp == MIN_WORD) {
            throw new IllegalArgumentException
                    ("Gsm_Long_Term_Synthesis_Filtering brp = "
                            +brp+" is out of range. Should be = " +MIN_WORD);
        }
        for (int k = 0; k <= 39; k++) {
            drpp = GSM_MULT_R( brp, dp0[ k - Nr + dp0_index_start_drp ] );
            dp0[ k + dp0_index_start_drp ] = GSM_ADD( erp[k], drpp );
        }
        /*
         *  Update of the reconstructed short term residual signal
         *  drp[ -1..-120 ]
         */
        System.arraycopy( dp0, (dp0_index_start_drp - 80),
                dp0, (dp0_index_start_drp - 120), 120 );
    }

    private int[] Gsm_Short_Term_Synthesis_Filter(
            short[] LARcr,        /* received log area ratios [0..7] IN  */
            short[] wt)           /* received d [0..159]             IN  */
            throws ArrayIndexOutOfBoundsException {

        short[] LARp = new short[8];
        int[] s = new int[160];

        short[] LARpp_j       = LARpp[ j ];
        short[] LARpp_j_1     = LARpp[ j^=1 ];

        Decoding_of_the_coded_Log_Area_Ratios( LARcr, LARpp_j );

        Coefficients_0_12( LARpp_j_1, LARpp_j, LARp );
        LARp_to_rp( LARp );
        Short_term_synthesis_filtering( LARp, 13, wt, s, 0 );

        Coefficients_13_26( LARpp_j_1, LARpp_j, LARp);
        LARp_to_rp( LARp );
        Short_term_synthesis_filtering( LARp, 14, wt, s, 13 );

        Coefficients_27_39( LARpp_j_1, LARpp_j, LARp);
        LARp_to_rp( LARp );
        Short_term_synthesis_filtering( LARp, 13, wt, s, 27 );

        Coefficients_40_159( LARpp_j, LARp );
        LARp_to_rp( LARp );
        Short_term_synthesis_filtering( LARp, 120, wt, s, 40);

        return s;
    }

    private static void Decoding_of_the_coded_Log_Area_Ratios(
            short[]  LARc,         /* coded log area ratio [0..7]  IN      */
            short[]  LARpp)        /* out: decoded ..                      */
    {
        short   temp1 = 0, temp2 = 0;
        int     index = 0;

        /*  This procedure requires for efficient implementation
         *  two tables.
         *
         *  INVA[1..8] = integer( (32768 * 8) / real_A[1..8])
         *  MIC[1..8]  = minimum value of the LARc[1..8]
         */

        /*  Compute the LARpp[1..8]
         */

        STEP(LARc, LARpp, index++, temp1, (short)0, (short) -32, (short) 13107);
        STEP(LARc, LARpp, index++, temp1, (short)0, (short) -32, (short) 13107);
        STEP(LARc, LARpp, index++, temp1, (short)2048, (short)-16, (short)13107);
        STEP(LARc, LARpp, index++, temp1, (short)-2560, (short)-16, (short)13107);

        STEP(LARc, LARpp, index++, temp1, (short) 94, (short) -8, (short) 19223);
        STEP(LARc, LARpp, index++, temp1, (short) -1792, (short)-8, (short)17476);
        STEP(LARc, LARpp, index++, temp1, (short) -341, (short)-4, (short)31454);
        STEP(LARc, LARpp, index++, temp1, (short) -1144, (short)-4, (short)29708);

        /* NOTE: the addition of *MIC is used to restore
         *       the sign of *LARc.
         */
    }

    private static void STEP(short[] LARc, short[] LARpp, int index,
                             short temp1, short B, short MIC, short INVA)
    {
        temp1    = (short) (GSM_ADD( LARc[index], MIC ) << 10);
        temp1    = GSM_SUB( temp1, (short) (B << 1) );
        temp1    = GSM_MULT_R( INVA, temp1 );
        LARpp[index] = GSM_ADD( temp1, temp1 );
    }

    /*
     *  Within each frame of 160 analyzed speech samples the short term
     *  analysis and synthesis filters operate with four different sets of
     *  coefficients, derived from the previous set of decoded LARs(LARpp(j-1))
     *  and the actual set of decoded LARs (LARpp(j))
     *
     * (Initial value: LARpp(j-1)[1..8] = 0.)
     */
    private static void Coefficients_0_12(
            short[] LARpp_j_1,
            short[] LARpp_j,
            short[] LARp)
    {
        for (int i = 0; i < 8; i++) {
            LARp[i] = GSM_ADD(SASR(LARpp_j_1[i], 2 ),
                    SASR(LARpp_j[i], 2 ));

            LARp[i] = GSM_ADD(LARp[i],
                    SASR(LARpp_j_1[i], 1));
        }
    }

    private static void Coefficients_13_26(
            short[] LARpp_j_1,
            short[] LARpp_j,
            short[] LARp)
    {
        for (int i = 0; i < 8; i++) {
            LARp[i] = GSM_ADD(SASR(LARpp_j_1[i], 1),
                    SASR(LARpp_j[i], 1 ));
        }
    }


    private static void Coefficients_27_39(
            short[] LARpp_j_1,
            short[] LARpp_j,
            short[] LARp)
    {
        for (int i = 0; i < 8; i++) {
            LARp[i] = GSM_ADD(SASR(LARpp_j_1[i], 2 ),
                    SASR(LARpp_j[i], 2 ));

            LARp[i] = GSM_ADD(LARp[i],
                    SASR(LARpp_j[i], 1 ));
        }
    }


    private static void Coefficients_40_159(
            short[] LARpp_j,
            short[] LARp)
    {
        for (int i = 0; i < 8; i++)
            LARp[i] = LARpp_j[i];
    }

    /*
     *  The input of this method is the interpolated LARp[0..7] array.
     *  The reflection coefficients, rp[i], are used in the analysis
     *  filter and in the synthesis filter.
     */
    private static void LARp_to_rp(
            short[] LARp)   /* [0..7] IN/OUT  */
    {
        short           temp;

        for (int i = 0; i < 8; i++) {

            if (LARp[i] < 0) {
                temp = (short) (LARp[i] == MIN_WORD ?
                        MAX_WORD : -(LARp[i]));

                LARp[i] = (short) (- ((temp < 11059) ? temp << 1
                        : ((temp < 20070) ? temp + 11059
                        :  GSM_ADD( (short) (temp >> 2),
                        (short) 26112 ))));
            } else {
                temp  = LARp[i];
                LARp[i] = (short) ((temp < 11059) ? temp << 1
                        : ((temp < 20070) ? temp + 11059
                        :  GSM_ADD( (short) (temp >> 2),
                        (short) 26112 )));
            }
        }
    }

    private void Short_term_synthesis_filtering(
            short[] rrp,    /* [0..7]       IN      */
            int     k,      /* k_end - k_start      */
            short[] wt,     /* [0..k-1]     IN      */
            int[]   sr,     /* [0..k-1]     OUT     */
            int     wt_sr_index_start)
    {
        short   sri = 0, tmp1 = 0, tmp2 = 0;
        int     ltmp = 0;   /* for GSM_ADD  & GSM_SUB */
        int     index = wt_sr_index_start;

        while (k != 0) {
            k--;
            sri = wt[index];
            for (int i = 7; i >= 0; i--) {

                /* sri = GSM_SUB( sri, gsm_mult_r( rrp[i],
                 *                v[i] ) );
                 */

                tmp1 = rrp[i];
                tmp2 = v[i];
                tmp2 = (short) ( tmp1 == MIN_WORD &&
                        tmp2 == MIN_WORD
                        ? MAX_WORD
                        : 0x0FFFF & (( (int)tmp1 * (int)tmp2
                        + 16384) >> 15)) ;

                sri  = GSM_SUB( sri, tmp2 );

                /* v[i+1] = GSM_ADD( v[i],
                 *                   gsm_mult_r( rrp[i], sri ) );
                 */

                tmp1  = (short) ( tmp1 == MIN_WORD &&
                        sri == MIN_WORD
                        ? MAX_WORD
                        : 0x0FFFF & (( (int)tmp1 * (int)sri
                        + 16384) >> 15)) ;

                v[i+1] = GSM_ADD( v[i], tmp1);
            }
            sr[index++] = v[0] = sri;
        }
    }


    private final void Postprocessing( int[] signal )
    {
        short   tmp;	 /* for GSM_ADD */
        int i = 0;
        for (int k = 160; k-- > 0; i++) {
            tmp = GSM_MULT_R(msr, (short) 28180);
            /* Deemphasis  */
            msr  = GSM_ADD( (short) signal[i], tmp);
            /* Truncation & Upscaling */
            signal[i] = (GSM_ADD(msr, msr) & 0xFFF8);
        }
    }

    private final static short GSM_MULT_R(short a, short b) {
        if (a == MIN_WORD && b == MIN_WORD) {
            return MAX_WORD;
        }
        else {
            int prod = (int) (((int)(a)) * ((int)(b)) + 16384);
            prod >>= 15;
            return (short) (prod & 0xFFFF);
        }
    }

    private final static short SASR(int x, int by) {
        return (short) ((x) >> (by));
    }

    private final static short GSM_ADD(short a, short b) {
        int sum = a + b;
        return saturate(sum);
    }

    private final static short GSM_SUB(short a, short b) {
        int diff = a - b;
        return saturate(diff);
    }

    private final static short gsm_asl(short a, int n)
    {
        if (n >= 16) return ((short) 0);

        if (n <= -16)
            if(a<0)
                return (short) -1;
            else
                return (short) -0;

        if (n < 0) return gsm_asr(a, -n);

        return ((short)( a << n ));
    }

    private final static short gsm_asr(short a, int n)
    {
        if (n >= 16)
            if(a<0)
                return (short) -1;
            else
                return (short) -0;
        if (n <= -16) return ((short) 0);
        if (n < 0) return ((short)( a << -n ));

        return ((short)( a >> n ));
    }


    public static short saturate(int x) {
        return (short) ((x) < MIN_WORD ? MIN_WORD :
                (x) > MAX_WORD ? MAX_WORD: (x));
    }


}
