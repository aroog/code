DIGITS_ALLSYSTEMS = 0

source(file="./metrics_lib.R")
#NOTE: Exclude: "All_NumEdges"
metricNames = c("All_AllTypes", "All_InstantiatiableTypes", "All_AbstractClasses", "All_Interfaces", "All_NumObjects", "All_NumPtEdges")

all_table<- sapply(metricNames, getMetric, table)
all_table<-setDisplayNames(metricNames, all_table)
#Do not transpose table here.
#all_table<- t(all_table)
LONG_CAPTION = "All systems"
SHORT_CAPTION = "All systems"
LABEL  = "tab:all_systems"
printLatexTable(all_table, digits=DIGITS_ALLSYSTEMS, caption.long = LONG_CAPTION, caption.short= SHORT_CAPTION, label=LABEL, file = "./all_systems.tex")
