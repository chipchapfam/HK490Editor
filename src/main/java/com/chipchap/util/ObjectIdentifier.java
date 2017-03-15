package com.chipchap.util;

/**
 * general object for all key words from channel file
 *
 * @author Chi Thien Phan
 * @version 1.0
 * @since 15.03.2017
 *
 */
public final class ObjectIdentifier {
	public static final String SAT_ID = "satellite_object_";
	public static final String TRANS_ID = "transponder_object_";
	public static final String TV_ID = "program_tv_object_";
	public static final String RADIO_ID = "program_radio_object_";
	public static final String BOX_ID = "box_object";
	public static final String WATCHING_ID = "watching_prog_object";
	public static final String FAV_ID = "fav_list_object_";
	public static final String FAV_LIST_ID = "fav_list_info_in_box_object";
	public static final String DB_HEADER_ID = "database_header_object";
	public static final String GLOBAL_ID = "global_variable_object";
	public static final String AUCFAVRENAME_ID = "aucFavReName";

	public static final String STPROGNO = "stProgNo";
	public static final String UIWORD32 = "uiWord32";
	public static final String UIMARK = "uiMark";
	public static final String SRAPSNO = "sRapsNo";
	public static final String[] DEFAULT_PROPS = { "sNoOfTVFavor", "sNoOfRadioFavor", "sTailOfFavor", "bUpdateFavor",
			"cHide" };

	public static final String ACSERVICENAME = "acServiceName";
	public static final String UCFAVOURITETYPE = "ucFavouriteType";
	public static final String STVNUMBER = "sTVNumber";
	public static final String SMAXFAVOR = "sMaxFavor";

	public static final String USFAVSELECT = "usFavSelect";

	public static final double PROGRESS_MAX = 10000.0;
}
