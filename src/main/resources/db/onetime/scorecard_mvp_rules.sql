INSERT INTO tdq.t_scorecard_groovy_rule_def(name, metric_keys, category, sub_category1, sub_category2, execute_order,
                                            default_weight, groovy_script)
VALUES ('Global tag guid rate', 'guid_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($guid_coverage = 100) {
  return 100
} else {
  return 50
}'),
       ('Global tag pageid rate', 'pageid_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($pageid_coverage = 100) {
    return 100
} else {
    return 50
}'),
       ('Global tag siteid rate', 'siteid_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($siteid_coverage = 100) {
    return 100
} else {
    return 50
}'),
       ('Native tag mav rate', 'native_mav_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_mav_coverage = 100) {
    return 100
} else if ($native_mav_coverage >= 80 && $native_mav_coverage < 100) {
    return 80
} else if ($native_mav_coverage >= 60 && $native_mav_coverage < 80) {
    return 60
} else if ($native_mav_coverage >= 40 && $native_mav_coverage < 60) {
    return 40
} else {
    return 0
}'),
       ('Native tag dn rate', 'native_dn_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_dn_coverage = 100) {
    return 100
} else if ($native_dn_coverage >= 80 && $native_dn_coverage < 100) {
    return 80
} else if ($native_dn_coverage >= 60 && $native_dn_coverage < 80) {
    return 60
} else if ($native_dn_coverage >= 40 && $native_dn_coverage < 60) {
    return 40
} else {
    return 0
}'),
       ('Native tag mos rate', 'native_mos_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_mos_coverage = 100) {
    return 100
} else if ($native_mos_coverage >= 80 && $native_mos_coverage < 100) {
    return 80
} else if ($native_mos_coverage >= 60 && $native_mos_coverage < 80) {
    return 60
} else if ($native_mos_coverage >= 40 && $native_mos_coverage < 60) {
    return 40
} else {
    return 0
}'),
       ('Native tag osv rate', 'native_osv_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_osv_coverage = 100) {
    return 100
} else if ($native_osv_coverage >= 80 && $native_osv_coverage < 100) {
    return 80
} else if ($native_osv_coverage >= 60 && $native_osv_coverage < 80) {
    return 60
} else if ($native_osv_coverage >= 40 && $native_osv_coverage < 60) {
    return 40
} else {
    return 0
}'),
       ('Native tag tzname rate', 'native_tzname_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_tzname_coverage >= 80 && $native_tzname_coverage <= 100) {
    return 100
} else if ($native_tzname_coverage >= 50 && $native_tzname_coverage < 80) {
    return 80
} else if ($native_tzname_coverage >= 30 && $native_tzname_coverage < 50) {
    return 50
} else if ($native_tzname_coverage >= 10 && $native_tzname_coverage < 30) {
    return 30
} else {
    return 0
}'),
       ('Native tag carrier rate', 'native_carrier_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_carrier_coverage >= 80 && $native_carrier_coverage <= 100) {
    return 100
} else if ($native_carrier_coverage >= 50 && $native_carrier_coverage < 80) {
    return 80
} else if ($native_carrier_coverage >= 30 && $native_carrier_coverage < 50) {
    return 50
} else if ($native_carrier_coverage >= 10 && $native_carrier_coverage < 30) {
    return 30
} else {
    return 0
}'),
       ('Native tag tz rate', 'native_tz_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_tz_coverage >= 80 && $native_tz_coverage <= 100) {
    return 100
} else if ($native_tz_coverage >= 50 && $native_tz_coverage < 80) {
    return 80
} else if ($native_tz_coverage >= 30 && $native_tz_coverage < 50) {
    return 50
} else if ($native_tz_coverage >= 10 && $native_tz_coverage < 30) {
    return 30
} else {
    return 0
}'),
       ('Native tag mnt rate', 'native_mnt_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($native_mnt_coverage >= 80 && $native_mnt_coverage <= 100) {
    return 100
} else if ($native_mnt_coverage >= 50 && $native_mnt_coverage < 80) {
    return 80
} else if ($native_mnt_coverage >= 30 && $native_mnt_coverage < 50) {
    return 50
} else if ($native_mnt_coverage >= 10 && $native_mnt_coverage < 30) {
    return 30
} else {
    return 0
}'),
       ('Experimental tag ec rate', 'ep_ec_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($ep_ec_coverage >= 99) {
    return 100
} else {
    return 50
}'),
       ('Experimental tag es rate', 'ep_es_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($ep_es_coverage >= 99) {
    return 100
} else {
    return 50
}'),
       ('Experimental tag nqc rate', 'ep_nqc_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($ep_nqc_coverage >= 99) {
    return 100
} else {
    return 50
}'),
       ('Experimental tag nqt rate', 'ep_nqt_coverage', 'COMPLETENESS', null, null, null, 1, 'if ($ep_nqt_coverage >= 99) {
    return 100
} else {
    return 50
}');
