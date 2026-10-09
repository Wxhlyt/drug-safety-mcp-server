import test from 'node:test'
import assert from 'node:assert/strict'
import { formatSourceTerm } from '../src/utils/sourceTranslations.js'

test('FRDB relationship and event values retain English and append Chinese', () => {
  assert.equal(formatSourceTerm('inhibitor', 'ddiRelation'), 'inhibitor（抑制剂）')
  assert.equal(formatSourceTerm('substrate', 'ddiRelation'), 'substrate（底物）')
  assert.equal(formatSourceTerm('discontinuation', 'adverseType'), 'discontinuation（停止治疗）')
  assert.equal(
    formatSourceTerm('discontinuation|dose interruption', 'adverseType'),
    'discontinuation（停止治疗） | dose interruption（暂时中断给药）'
  )
})

test('severity grade and source metadata have distinct display translations', () => {
  assert.equal(formatSourceTerm('grade 3-4', 'adverseSeverity'), 'grade 3-4（3至4级）')
  assert.equal(formatSourceTerm('serious', 'adverseSeverity'), 'serious（严重不良事件）')
  assert.equal(formatSourceTerm('severe', 'adverseSeverity'), 'severe（程度严重）')
  assert.equal(formatSourceTerm('MATCHED', 'mappingStatus'), 'MATCHED（唯一匹配）')
  assert.equal(formatSourceTerm('TABLET, FILM COATED', 'dosageForm'), 'TABLET, FILM COATED（薄膜衣片）')
  assert.equal(formatSourceTerm('14 pts', 'adverseFrequency'), '14 pts（14名患者）')
  assert.equal(formatSourceTerm('rare', 'adverseFrequency'), 'rare（罕见）')
  assert.equal(formatSourceTerm('16.7', 'adverseFrequency'), '16.7')
})

test('fixed codes and unverified source text remain unchanged', () => {
  assert.equal(formatSourceTerm('CYP2C19', 'ddiRelation'), 'CYP2C19')
  assert.equal(formatSourceTerm('IC50', 'ddiType'), 'IC50')
  assert.equal(formatSourceTerm('unmapped source phrase', 'ddiEvidence'), 'unmapped source phrase')
  assert.equal(formatSourceTerm('', 'adverseType'), '')
  assert.equal(formatSourceTerm(null, 'adverseType'), '')
})
