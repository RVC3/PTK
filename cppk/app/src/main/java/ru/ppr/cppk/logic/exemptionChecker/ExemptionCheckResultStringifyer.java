package ru.ppr.cppk.logic.exemptionChecker;

import android.content.Context;

import ru.ppr.cppk.R;

/**
 * Класс, выполняющий получение строкого представления ошибки использования льготы.
 *
 * @author Aleksandr Brazhkin
 */
public class ExemptionCheckResultStringifyer {

    private final Context context;

    public ExemptionCheckResultStringifyer(Context context) {
        this.context = context;
    }

    public String getString(ExemptionChecker.CheckResult result, int exemptionExpressCode, String ticketTypeName) {

        String errorMessage;

        switch (result) {
            case INVALID_EXPRESS_CODE:
                errorMessage = context.getString(R.string.exemption_check_result_invalid_express_code);
                break;

            case INVALID_CARD_ID:
                errorMessage = context.getString(R.string.exemption_check_result_invalid_card_id);
                break;

            case IN_CPPK_REGISTRY_BAN:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_in_cppk_registry_ban), exemptionExpressCode);
                break;

            case INVALID_PERIOD:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_invalid_period), exemptionExpressCode);
                break;

            case DENIED_FOR_REGION:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_region), exemptionExpressCode);
                break;

            case BANNED_DEVICE:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_banned_device), exemptionExpressCode);
                break;

            case DENIED_FOR_TARIFF_PLAN:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_tariff_plan), exemptionExpressCode);
                break;

            case DENIED_FOR_MANUAL_INPUT:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_manual_input), exemptionExpressCode);
                break;

            case SOCIAL_CARD_REQUIRED:
                errorMessage = context.getString(R.string.exemption_check_result_social_card_required);
                break;

            case DENIED_FOR_TICKET_STORAGE_TYPE:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_ticket_storage_type), exemptionExpressCode, ticketTypeName);
                break;

            case DENIED_FOR_TICKET_TYPE:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_ticket_type), exemptionExpressCode);
                break;

            case DENIED_FOR_CHILD_TICKET:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_child_ticket), exemptionExpressCode);
                break;

            case DENIED_FOR_REPEATED_SALE:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_repeated_sale), exemptionExpressCode);
                break;

            case DENIED_FOR_BENEFICIARY_CATEGORY:
                errorMessage = String.format(context.getString(R.string.exemption_check_result_denied_for_beneficiary_category), ticketTypeName);
                break;

            case DENIED_FOR_EXTRA_SALE_FOR_SEASON_TICKET_ON_ETT:
                errorMessage = context.getString(R.string.exemption_check_result_denied_for_extra_sale_for_season_ticket_on_ett);
                break;

            default:
                throw new IllegalArgumentException("Unknown check result");
        }

        return errorMessage;
    }
}
