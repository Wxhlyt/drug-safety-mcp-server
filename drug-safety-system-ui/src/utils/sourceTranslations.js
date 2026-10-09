// 只翻译已核对含义的来源字段值；英文原值保留，便于与 FRDB / DailyMed 核对。
// 未收录的自由文本和固定标识原样显示，不能用猜测的中文替代来源记录。
const terms = {
  ddiRelation: {
    activator: '激活剂',
    binder: '靶点结合物',
    inducer: '诱导剂',
    inhibitor: '抑制剂',
    substrate: '底物',
    suppressor: '表达抑制物'
  },
  ddiType: {
    Activation: '激活率',
    Inhibition: '抑制率'
  },
  ddiEvidence: {
    'biomarker study': '生物标志物研究',
    'co-administration study': '联合给药研究',
    'expression study': '表达量研究',
    'pharmacogenomic study': '药物基因组学研究',
    'QT study': 'QT 间期研究'
  },
  adverseType: {
    discontinuation: '停止治疗',
    'dose interruption': '暂时中断给药',
    'dose reduction': '降低剂量'
  },
  adverseSeverity: {
    acute: '急性',
    'all grades': '所有级别',
    'below serious': '非严重类别',
    extreme: '极重',
    light: '较轻',
    major: '重度',
    marked: '明显',
    mild: '轻度',
    moderate: '中度',
    'non-serious': '非严重',
    serious: '严重不良事件',
    severe: '程度严重',
    significant: '显著',
    slight: '轻微',
    uncertain: '不确定'
  },
  adverseFrequency: {
    rare: '罕见',
    uncommon: '不常见',
    common: '常见',
    infrequent: '不常见',
    'very rare': '非常罕见',
    'low incidence': '发生率低',
    sometimes: '有时发生',
    'most common': '最常见'
  },
  mappingStatus: {
    MATCHED: '唯一匹配',
    AMBIGUOUS: '存在多个候选',
    UNMATCHED: '未匹配',
    NEEDS_REVIEW: '待复核'
  },
  matchMethod: {
    EXACT_UNII: 'UNII 精确匹配',
    RXNORM_NORMALIZED_NAME: 'RxNorm 标准化名称匹配',
    PENDING: '待匹配'
  },
  dataQuality: {
    FRDB_SOURCE_ONLY: '仅有 FRDB 来源基础数据'
  },
  dosageForm: {
    CAPSULE: '胶囊剂',
    'CAPSULE, COATED PELLETS': '包衣微丸胶囊',
    'CAPSULE, EXTENDED RELEASE': '缓释胶囊',
    'CAPSULE, GELATIN COATED': '明胶包衣胶囊',
    'CAPSULE, LIQUID FILLED': '液体填充胶囊',
    CREAM: '乳膏剂',
    EXTRACT: '提取物',
    'FOR SOLUTION': '供配制溶液用',
    GEL: '凝胶剂',
    GRANULE: '颗粒剂',
    IMPLANT: '植入剂',
    INHALANT: '吸入剂',
    INJECTION: '注射剂',
    'INJECTION, POWDER, FOR SOLUTION': '供配制注射溶液的粉末',
    'INJECTION, POWDER, LYOPHILIZED, FOR SOLUTION': '供配制注射溶液的冻干粉末',
    'INJECTION, SOLUTION': '注射液',
    'INJECTION, SOLUTION, CONCENTRATE': '注射用浓溶液',
    KIT: '组合包装',
    LIQUID: '液体制剂',
    LOTION: '洗剂',
    OIL: '油剂',
    OINTMENT: '软膏剂',
    'PATCH, EXTENDED RELEASE': '缓释贴剂',
    PELLET: '微丸',
    POWDER: '粉剂',
    'POWDER, FOR SOLUTION': '供配制溶液的粉末',
    'POWDER, FOR SUSPENSION': '供配制混悬液的粉末',
    RING: '环形给药制剂',
    SHAMPOO: '洗发剂',
    SOLUTION: '溶液剂',
    'SOLUTION/ DROPS': '滴剂溶液',
    SPRAY: '喷雾剂',
    'SPRAY, METERED': '定量喷雾剂',
    'SPRAY, SUSPENSION': '混悬型喷雾剂',
    STRIP: '薄膜条',
    SUSPENSION: '混悬剂',
    'SUSPENSION/ DROPS': '滴剂混悬液',
    SYRUP: '糖浆剂',
    TABLET: '片剂',
    'TABLET, COATED': '包衣片',
    'TABLET, EXTENDED RELEASE': '缓释片',
    'TABLET, FILM COATED': '薄膜衣片',
    'TABLET, FILM COATED, EXTENDED RELEASE': '薄膜衣缓释片',
    'TABLET, FOR SUSPENSION': '供配制混悬液的片剂',
    'TABLET, SOLUBLE': '可溶片'
  }
}

const compoundFields = new Set(['ddiEvidence', 'adverseType', 'adverseSeverity'])

export const formatSourceTerm = (value, field) => {
  if (value === null || value === undefined || value === '') return value ?? ''
  const raw = String(value)
  const dictionary = terms[field]
  if (!dictionary) return raw

  if (compoundFields.has(field) && raw.includes('|')) {
    return raw.split('|').map(part => formatSourceTerm(part.trim(), field)).join(' | ')
  }

  const grade = field === 'adverseSeverity' && /^grade ([1-5])(?:-([1-5]))?$/i.exec(raw)
  const patients = field === 'adverseFrequency' && /^([<>]=?\s*)?(\d+)\s*pts$/i.exec(raw)
  const patientPrefix = {
    '>': '超过', '>=': '至少', '<': '少于', '<=': '至多'
  }[patients?.[1]?.trim()] || ''
  const translation = grade
    ? grade[2] ? `${grade[1]}至${grade[2]}级` : `${grade[1]}级`
    : patients ? `${patientPrefix}${patients[2]}名患者` : dictionary[raw]
  return translation ? `${raw}（${translation}）` : raw
}
