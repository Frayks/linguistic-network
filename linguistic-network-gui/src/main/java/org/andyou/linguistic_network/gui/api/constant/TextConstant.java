package org.andyou.linguistic_network.gui.api.constant;

public interface TextConstant {

    String WARNING_MESSAGE_MISSING_STOP_WORDS = "You selected option \"Remove stop words\", but didn't select a file with stop words!";
    String WARNING_MESSAGE_NO_RESTRICTIVE_OPTION_IS_SELECTED = "You have not selected either the \"Bounds\" or \"Use range\" option.\nIt means that all elements will be considered neighbors!\nProcessing such a large number of connections can be time-consuming!\nDo you want to continue?";
    String WARNING_MESSAGE_FILE_ALREADY_EXISTS = "%s already exists.\nDo you want to replace it?";
    String INFORMATION_MESSAGE_CHOOSE_SAVING_FORMAT = "Choose the format for saving statistics.";
    String TITLE_SAVE_AS = "Save as...";
    String TITLE_ERROR = "Error";
    String TITLE_WARNING = "Warning";

}
