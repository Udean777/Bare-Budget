package i18n

import "fmt"

// In-memory localized message dictionary supporting English and Indonesian string templates.
var messages = map[string]map[string]string{
	"en": {
		"runway.no_budget":    "Monthly budget is not set yet. Set your budget to track your financial runway!",
		"runway.exhausted":    "Runway exhausted! Your budget was exceeded on day %d.",
		"runway.will_run_out": "At your current spending rate (Rp %s/day), your budget will run out on day %d!",
		"runway.safe":         "Your financial runway is safe until the end of the month. Keep it up!",
		"runway.no_expenses":  "No expenses recorded this month yet. Your budget is untouched!",
	},
	"id": {
		"runway.no_budget":    "Budget bulanan belum diatur. Atur budget untuk melacak runway finansialmu!",
		"runway.exhausted":    "Runway habis! Budget kamu terlewati pada hari ke-%d.",
		"runway.will_run_out": "Dengan laju pengeluaran saat ini (Rp %s/hari), budget akan habis pada hari ke-%d!",
		"runway.safe":         "Runway finansial aman hingga akhir bulan. Pertahankan!",
		"runway.no_expenses":  "Belum ada pengeluaran bulan ini. Budget masih utuh!",
	},
}

func T(lang, key string, args ...interface{}) string {
	if m, ok := messages[lang]; ok {
		if tmpl, ok := m[key]; ok {
			if len(args) > 0 {
				return fmt.Sprintf(tmpl, args...)
			}
			return tmpl
		}
	}
	// fallback en
	if tmpl, ok := messages["en"][key]; ok {
		if len(args) > 0 {
			return fmt.Sprintf(tmpl, args...)
		}
		return tmpl
	}
	return key
}
