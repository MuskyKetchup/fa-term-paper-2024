#let uni = [
  Федеральное государственное образовательное бюджетное  
  учреждение высшего образования \
  #text(17pt)[
    *"Финансовый университет 
    при Правительстве Российской Федерации" \
    (Финансовый университет)*
  ]
]

#let dep = [
  Департамент анализа данных и машинного обучения 
]

#let papertype = [
  Пояснительная записка к курсовой работе по дисциплине 
  "Современные технологии программирования"  
  на тему:
]

#let purpose = [
 "Нейросетевая система прогнозирования электроэнергии"
]

#set page(
  paper: "a4",
  header: align(center)[
  ],
  footer: align(center)[
    *Москва 2024*
  ],
  numbering: "1",
)
#set par(justify: true)
#set text(
  font: "Libertinus Serif",
  size: 15pt,
)
#align(horizon)[
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
#v(20%,weak: true)
#align(horizon)[
#grid(
  columns: (1fr, 1fr),
  align(center)[
  ],
  align(right)[
    Выполнил: \
    Студент группы ПИ21-7 \
    Пятунин А. И. \
    \
    \
    Научный руководитель: \
    к.т.н., ст. преподаватель \
    Пальчевский Е.В. \
  ]
)
]
#pagebreak()

#lorem(600)

