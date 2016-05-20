#TODO: Do not set caption or caption.placement if generating only content
DIGITS = 0

# Specify number of significant digits
options(digits=3)
# Increase penalty for displaying numbers in fixed or exponential notation
options(scipen=10)

source(file="./metrics_lib.R")

#selected metrics to be included in the latex file

#TODO: Instead of duplicating metric names, could move this later, and concatenate the 3 vectors into 1
#all metrics
#CUT: EBO for now: "EBO_W_Median","EBO_W_Max"
metricNames <- c("WA_Cluster_Median","WA_Cluster_Max","WAR_Cluster_Median","WAR_Cluster_Max","WAB_Cluster_Median","WAB_Cluster_Max","WABR_Cluster_Median","WABR_Cluster_Max","WAWB_Cluster_Median","WAWB_Cluster_Max","WAWBR_Cluster_Median","WAWBR_Cluster_Max","HMO_Cluster_Median","HMO_Cluster_Max","X1FnE_N_Median","X1FnE_N_Max","HMN_Cluster_Median","HMN_Cluster_Max","TMO_N_Median","TMO_N_Max","TOS_N_Median","TOS_N_Max","X1PnD_Cluster_Median","X1PnD_Cluster_Max","X1DnP_Cluster_Median","X1DnP_Cluster_Max","InhD_P","InhE_P","PO_P","SO_F_Median","SO_F_Max","PTEP_F_Median", "PTEP_F_Max", "DFEP_F_Median", "DFEP_F_Max", "X1MInE_N_Median","X1MInE_N_Max", "X1MInE_RecType_N_Median","X1MInE_RecType_N_Max", "X1MInE_ArgType_N_Median","X1MInE_ArgType_N_Max", "X1MInE_RetType_N_Median","X1MInE_RetType_N_Max", "X1FRnE_N_Median","X1FRnE_RecType_N_Max", "X1FRnE_RecType_N_Median","X1FRnE_N_Max", "X1FWnE_N_Median","X1FWnE_N_Max", "X1FWnE_RecType_N_Median","X1FWnE_RecType_N_Max")
result <- getSubsetOfSummaryTable(metricNames, doMultiplier=TRUE)
LONG_CAPTION = "Selected metrics shown across all systems"
SHORT_CAPTION = "All metrics"
LABEL  = "tab:all_metrics"
printLatexTable(result, digits=DIGITS, caption.long = LONG_CAPTION, caption.short= SHORT_CAPTION, label=LABEL, file = "./all_metrics.tex")

#Order metrics: cluster metrics, object metrics, edge metrics:

#Cluster metrics
metricNames1 <- c("WA_Cluster_Median","WA_Cluster_Max","WAR_Cluster_Median","WAR_Cluster_Max","WAB_Cluster_Median","WAB_Cluster_Max","WABR_Cluster_Median","WABR_Cluster_Max","WAWB_Cluster_Median","WAWB_Cluster_Max","WAWBR_Cluster_Median","WAWBR_Cluster_Max","HMO_Cluster_Median","HMO_Cluster_Max","X1FnE_N_Median","X1FnE_N_Max","HMN_Cluster_Median","HMN_Cluster_Max","TMO_N_Median","TMO_N_Max","TOS_N_Median","TOS_N_Max","X1PnD_Cluster_Median","X1PnD_Cluster_Max","X1DnP_Cluster_Median","X1DnP_Cluster_Max")
result1 <- getSubsetOfSummaryTable(metricNames1)
LONG_CAPTION1 = "Cluster metrics across all systems"
SHORT_CAPTION1 = "Cluster metrics"
LABEL1  = "tab:cluster_metrics"
printLatexTable(result1, digits=DIGITS, caption.long = LONG_CAPTION1, caption.short= SHORT_CAPTION1, label=LABEL1, file = "./cluster_metrics.tex")

#Object metrics
metricNames2 <- c("InhD_P","PO_P","SO_F_Median","SO_F_Mean","SO_F_Max")
result2 <- getSubsetOfSummaryTable(metricNames2)
LONG_CAPTION2 = "Object metrics across all systems"
SHORT_CAPTION2 = "Object metrics"
LABEL2  = "tab:obj_metrics"
printLatexTable(result2, digits=DIGITS, caption.long = LONG_CAPTION2, caption.short= SHORT_CAPTION2, label=LABEL2, file = "./obj_metrics.tex")

#Edge metrics
#CUT: EBO for now: "EBO_W_Median","EBO_W_Max",
metricNames3 <- c("InhE_P","PTEP_F_Median","PTEP_F_Max","X1FnE_N_Median","X1FnE_N_Max",  "DFEP_F_Median", "DFEP_F_Max", "X1MInE_N_Median","X1MInE_N_Max", "X1MInE_RecType_N_Median","X1MInE_RecType_N_Max", "X1MInE_ArgType_N_Median","X1MInE_ArgType_N_Max", "X1MInE_RetType_N_Median","X1MInE_RetType_N_Max", "X1FRnE_N_Median","X1FRnE_N_Max", "X1FRnE_RecType_N_Median","X1FRnE_RecType_N_Max", "X1FWnE_N_Median","X1FWnE_N_Max", "X1FWnE_RecType_N_Median","X1FWnE_RecType_N_Max")
result3 <- getSubsetOfSummaryTable(metricNames3)
LONG_CAPTION3 = "Edge metrics across all systems"
SHORT_CAPTION3 = "Edge metrics"
LABEL3  = "tab:edge_metrics"
printLatexTable(result3, digits=DIGITS, caption.long = LONG_CAPTION3, caption.short= SHORT_CAPTION3, label=LABEL3, file = "./edge_metrics.tex")

