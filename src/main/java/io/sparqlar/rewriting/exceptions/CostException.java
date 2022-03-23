/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sparqlar.rewriting.exceptions;

/**
 * @author riccardo
 */
public class CostException extends Exception {

    private CostException(String message) {
        super(message);
    }

    public static CostException buildCostException(boolean isMaxCost) {
        if (isMaxCost) {
            return new CostException("Every cost has to be greater than 0.");
        }
        return new CostException("Max cost has to be greater than or equal to 0.");
    }

}
