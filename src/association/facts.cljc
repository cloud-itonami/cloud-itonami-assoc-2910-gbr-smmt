(ns association.facts
  "Industry rule/history catalog for the Society of Motor Manufacturers
  and Traders (SMMT, UK) -- a 31st industry-association-level source
  per ADR-2607141700 (cloud-itonami-compliance-fact-federation). The
  SECOND entry aligned to ISIC 2910 (manufacture of motor vehicles),
  alongside cloud-itonami-assoc-2910-deu-vda (Germany's VDA) -- same
  cross-country-same-ISIC pattern already used for ISIC 6419
  (Zenginkyo/Bankenverband/FBF).

  Deliberately diversifies the association axis's country mix, which
  had skewed heavily USA (18 of 30 prior entries) and Japan (8 of 30)
  with only 2 EU entries and 0 UK entries before this tick.

  Both entries were directly WebFetch-verified against SMMT's own
  History page (smmt.co.uk/about/history/), which states plainly:
  'On 22 July 1902 the Society of Motor Manufacturers and Traders
  (SMMT) was created', and separately: 'the first SMMT exhibition was
  held at Crystal Palace in January 1903' (only month/year given for
  the exhibition, so :established-date is stored year-only, matching
  the year-only-date discipline already used for several sibling
  association entries). The same page's 'About' companion page names
  the sitting President and Chief Executive -- read only to confirm
  the governance-structure description, personal names never stored.

  A rule not in this table has NO spec-basis, full stop; extend
  `catalog`, do not invent an id/url.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"smmt"
   [{:association-rule/id "smmt.founding-1902"
     :association-rule/title "SMMT founding (History)"
     :association-rule/association "smmt"
     :association-rule/isic "2910"
     :association-rule/country "GBR"
     :association-rule/kind :governance-program
     :association-rule/url "https://www.smmt.co.uk/about/history/"
     :association-rule/url-provenance :official-smmt-co-uk
     :association-rule/established-date "1902-07-22"
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:governance}}
    {:association-rule/id "smmt.first-exhibition-1903"
     :association-rule/title "First SMMT motor exhibition at Crystal Palace (History)"
     :association-rule/association "smmt"
     :association-rule/isic "2910"
     :association-rule/country "GBR"
     :association-rule/kind :governance-program
     :association-rule/url "https://www.smmt.co.uk/about/history/"
     :association-rule/url-provenance :official-smmt-co-uk
     :association-rule/established-date "1903"
     :association-rule/retrieved-at "2026-07-16"
     :association-rule/topic #{:commerce}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-2910-gbr-smmt Wave 0 (ADR-2607141700): "
                 (count (get catalog "smmt")) " smmt entries seeded with an "
                 "official smmt.co.uk citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
