/*
 * Created by benoit.audigier on 7/10/2017 11:04 AM.
 */
package NeurTools;

public class RootCauseNodes extends Nodes {
    private String[] rootCauses;

    RootCauseNodes(String[] rootCauses) {
        super(null);
        this.rootCauses = rootCauses;
    }

    public String[] getRootCauses() {
        return rootCauses;
    }
}
