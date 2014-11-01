/* Created by R. Aidan Campbell on 11/1/14.

this is a state machine class to locate the char sequence '\r\n\r\n' (0D0A0D0A)
that signifies the end of the HTTP header
 */
public class TerminationStage {
    private enum TERMINATION_STAGE {reset,r1,n1,r2}
    private TERMINATION_STAGE termination_stage = TERMINATION_STAGE.reset;

    public boolean isHeaderEnded(char c){
        byte b = (byte) c;
        switch (termination_stage){
            case reset:
                if (b == 0x0D) termination_stage = TERMINATION_STAGE.r1;//move forward (0D)
                break;
            case r1:
                if(b == 0x0A) termination_stage = TERMINATION_STAGE.n1;//move forward (0D0A)
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;//repeated start character (0D0D)
                if(b != 0x0D && b != 0x0A) termination_stage = TERMINATION_STAGE.reset;//go back to start (0DFF)
                break;
            case n1:
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r2;//move forward (0D0A0D)
                else termination_stage = TERMINATION_STAGE.reset;//(0D0AFF)
                break;
            case r2:
                if(b == 0x0A) return true;//done (0D0A0D0A)
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;//repeated start character(0D0A0D0D)
                else termination_stage = TERMINATION_STAGE.reset;//(0D0A0DFF)
                break;
        }
        return false;
    }
}