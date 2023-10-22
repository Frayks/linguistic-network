package org.andyou.linguistic_network.gui.api.constant;

public interface MessageText {

    String WARNING_MESSAGE_MISSING_STOP_WORDS = "You selected option \"Remove stop words\", but didn't select a file with stop words!";
    String WARNING_MESSAGE_NOT_SELECTED_OPTIONS = "You have not selected either the \"Sentence bounds\" or \"Use range\" options.\nIt means that all words will be considered neighbors!\nProcessing such a large number of connections can be time-consuming!\nDo you want to continue?";

}
