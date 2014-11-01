/* Created by R. Aidan Campbell on 11/1/14.
 */
public class TerminationStage {
    private enum TERMINATION_STAGE {reset,r1,n1,r2}
    private TERMINATION_STAGE termination_stage = TERMINATION_STAGE.reset;

    public boolean isHeaderEnded(char c){
        byte b = (byte) c;
        switch (termination_stage){
            case reset:
                if (b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                break;
            case r1:
                if(b == 0x0A) termination_stage = TERMINATION_STAGE.n1;
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                if(b != 0x0D && b != 0x0A) termination_stage = TERMINATION_STAGE.reset;
                break;
            case n1:
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r2;
                if(b == 0x0A) termination_stage = TERMINATION_STAGE.r1;
                if(b != 0x0D && b != 0x0A) termination_stage = TERMINATION_STAGE.reset;
                break;
            case r2:
                if(b == 0x0A) return true;
                if(b == 0x0D) termination_stage = TERMINATION_STAGE.r1;
                else termination_stage = TERMINATION_STAGE.reset;
                break;
        }
        return false;
    }
}
