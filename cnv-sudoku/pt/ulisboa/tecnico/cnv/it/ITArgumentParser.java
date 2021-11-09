package pt.ulisboa.tecnico.cnv.it;

import org.apache.commons.cli.Option;
import org.json.JSONArray;
import org.json.JSONException;
import pt.ulisboa.tecnico.cnv.util.AbstractArgumentParser;


public class ITArgumentParser extends AbstractArgumentParser {
    public enum ITParameters {

        // we can define more if we need
        INPUT_ACTION("act"), INPUT_SERVER("srv"),
        INPUT_REGISTER("reg"), INPUT_PRT("prt");

        private final String text;
        ITParameters(final String text) {
            this.text = text;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return this.text;
        }
    }

    @Override
    public void parseValues(String[] args) {

        final String regStr = cmd.getOptionValue(ITParameters.INPUT_REGISTER.toString());
        if(regStr != null && !regStr.trim().equals("")){
            super.argValues.put(ITParameters.INPUT_REGISTER.toString(), regStr);
        }

            // DIRTY HACK: IF regStr is null, we have an updateInstances request
        final String actionStr = cmd.getOptionValue(ITParameters.INPUT_ACTION.toString());
        if(actionStr != null && !actionStr.trim().equals("")){
            super.argValues.put(ITParameters.INPUT_ACTION.toString(), actionStr);
        }

        final String serverStr = cmd.getOptionValue(ITParameters.INPUT_SERVER.toString());
        if(serverStr != null && !serverStr.trim().equals("")){
            super.argValues.put(ITParameters.INPUT_SERVER.toString(), serverStr);
        }

        final String prtStr = cmd.getOptionValue(ITParameters.INPUT_PRT.toString());
        if(prtStr != null && !prtStr.trim().equals("")){
            super.argValues.put(ITParameters.INPUT_PRT.toString(), prtStr);
        }


        // //N1
        // final Integer n1;
        // if(cmd.hasOption(ITParameters.INPUT_NR_COL.toString())){
        //     n1 = Integer.parseInt(cmd.getOptionValue(ITParameters.INPUT_NR_COL.toString()));
        //     if(n1 != jsonArray.length())
        //         throw new IllegalArgumentException(ITParameters.INPUT_NR_COL.toString() + " must be equal to the number of columns of the given boards: " + jsonArray.length()+".");
        //     super.argValues.put(ITParameters.INPUT_NR_COL.toString(), n1);
        // }else{
        //     n1 = jsonArray.length();
        //     super.argValues.put(ITParameters.INPUT_NR_COL.toString(), n1);
        // }
    }

    @Override
    public void setupCLIOptions() {

        // Mandatory arguments.

        final Option inputActionOption = new Option(ITParameters.INPUT_ACTION.toString(), ITParameters.INPUT_ACTION.toString(),true, "action to perform (add, rm)");
        // inputActionOption.setRequired(true);
        super.options.addOption(inputActionOption);

        final Option inputServerOption = new Option(ITParameters.INPUT_SERVER.toString(), ITParameters.INPUT_SERVER.toString(),true, "server IP to perform the action on");
        // inputServerOption.setRequired(true);
        super.options.addOption(inputServerOption);

        final Option inputRegOption = new Option(ITParameters.INPUT_REGISTER.toString(), ITParameters.INPUT_REGISTER.toString(),true, "server IP to perform the action on");
        // inputRegOption.setRequired(true);
        super.options.addOption(inputRegOption);

        final Option inputPrtOption = new Option(ITParameters.INPUT_PRT.toString(), ITParameters.INPUT_PRT.toString(),true, "server IP to perform the action on");
        // inputRegOption.setRequired(true);
        super.options.addOption(inputPrtOption);
    }

    public ITArgumentParser(final String[] args) {
        this.setup(args);
    }

    // public Integer getN1(){
    //     return (Integer)super.argValues.get(ITParameters.INPUT_NR_COL.toString());

   
    public String getInputServer() {
        return (String) super.argValues.get(ITParameters.INPUT_SERVER.toString());
    }

    public String getInputAction() {
        return (String) super.argValues.get(ITParameters.INPUT_ACTION.toString());
    }

    public String getInputReg() {
        return (String) super.argValues.get(ITParameters.INPUT_REGISTER.toString());
    }

    public String getInputPort() {
        return (String) super.argValues.get(ITParameters.INPUT_PRT.toString());
    }
}
