
// This is an example typst template (based on the default template that ships
// with Quarto). It defines a typst function named 'article' which provides
// various customization options. This function is called from the 
// 'typst-show.typ' file (which maps Pandoc metadata function arguments)
//
// If you are creating or packaging a custom typst template you will likely
// want to replace this file and 'typst-show.typ' entirely. You can find 
// documentation on creating typst templates and some examples here: 
//   - https://typst.app/docs/tutorial/making-a-template/
//   - https://github.com/typst/templates

#let dep = [
  Департамент анализа данных и машинного обучения 
]

#let papertype = [
  Пояснительная записка к курсовой работе по дисциплине \
  "Современные технологии программирования" \
  на тему:
]


#let uni = [
  Федеральное государственное образовательное бюджетное  
  учреждение высшего образования \
  #text(17pt)[
    *"Финансовый университет 
    при Правительстве Российской Федерации" \
    (Финансовый университет)*
  ]
]

#let article(
  title: none,
  subtitle: none,
  authors: none,
  date: none,
  abstract: none,
  abstract-title: none,
  cols: 1,
  margin: (x: 1.25in, y: 1.25in),
  paper: "a4",
  lang: "ru",
  region: "RU",
  font: "New Computer Modern",
  fontsize: 14pt,
  title-size: 1.5em,
  subtitle-size: 1.25em,
  heading-family: "New Computer Modern",
  heading-weight: "bold",
  heading-style: "normal",
  heading-color: black,
  heading-line-height: 0.65em,
  sectionnumbering: none,
  toc: true,
  toc_title: "Содержание",
  toc_depth: none,
  toc_indent: 1.5em,
  purpose: "Нейросетевая система прогнозирования электроэнергии",
  doc,
) = {
  set page(
    paper: paper,
    margin: margin,
    numbering: "1",
  )
  set par(justify: true)
  set text(lang: lang,
           region: region,
           font: font,
           size: fontsize)
  set heading(numbering: sectionnumbering)
set table(
  inset: 6pt,
  stroke: none
)
 if title != none {
  purpose = title
 } 

set page(
  paper: "a4",
  header: align(center)[
  ],
  footer: align(center)[
    *Москва 2024*
  ],
  numbering: "1",
)
set par(justify: true)
// set text(
//   font: "Libertinus Serif",
//   size: 15pt,
// )
align(horizon)[
#align(center)[
  #block(width: 90%)[
  #uni \
  \
  #dep \
  \
  #papertype \
  \
  *#purpose* \ 
]
]
]
v(20%,weak: true)
align(horizon)[
#grid(
  columns: (1fr, 1fr),
  align(center)[
  ],
  align(right)[
    Выполнил: \
    #if authors != none {
              authors.at(0).affiliation
    } \
    #if authors != none {
              authors.at(0).name 
    } \
    \
    Научный руководитель: \
    #if authors != none {
              authors.at(1).affiliation
    } \
    #if authors != none {
              authors.at(1).name 
    } \
    // к.т.н., ст. преподаватель \
    // Пальчевский Е.В. \
  ]
)
]
pagebreak()

  // if authors != none {
  //   let count = authors.len()
  //   let ncols = calc.min(count, 3)
  //   grid(
  //     columns: (1fr,) * ncols,
  //     row-gutter: 1.5em,
  //     ..authors.map(author =>
  //         align(center)[
  //           #author.name \
  //           #author.affiliation \
  //           #author.email
  //         ]
  //     )
  //   )
  // }

  if date != none {
    align(center)[#block(inset: 1em)[
      #date
    ]]
  }

  if abstract != none {
    block(inset: 2em)[
    #text(weight: "semibold")[#abstract-title] #h(1em) #abstract
    ]
  }

  if toc {
    let title = if toc_title == none {
      auto
    } else {
      toc_title
    }
    block(above: 0em, below: 2em)[
    #outline(
      title: toc_title,
      depth: toc_depth,
      indent: toc_indent
    );
    ]
  }

  if cols == 1 {
    doc
  } else {
    columns(cols, doc)
  }


}


