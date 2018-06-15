SELECT DISTINCT
  ConceptName, Image, RovName, DiveNumber
FROM
  Annotations
WHERE
  Image IS NOT NULL AND (
    ConceptName = ''
  ) AND (
    LinkValue = 'good' OR
    LinkValue = 'close-up' OR
    LinkValue = 'selects'
  )
ORDER BY
  ConceptName ASC