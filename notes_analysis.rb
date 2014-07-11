require 'json'
require 'axlsx'

puts "Student notes analysis. Starting...\n"

# Parse JSON files
json_5ag = JSON.parse(File.read("data/hg_fall_13/notes/hunger-games-fall-13-5ag_notes.json"))
json_5at = JSON.parse(File.read("data/hg_fall_13/notes/hunger-games-fall-13-5at_notes.json"))
json_5bj = JSON.parse(File.read("data/hg_fall_13/notes/hunger-games-fall-13-5bj_notes.json"))

# Dump to XLSX
def buildSheet (wb, json, name)
  wb.workbook.add_worksheet(:name => name) do |sheet|
    sheet.add_row ['author', 'created_at', 'part_1', 'part_2', 'related_activity', 'worth_remembering']
    json.each { |i|
      if i['created_at'] != nil
        d = DateTime.iso8601(i['created_at']['$date'])
        sheet.add_row [ i['author'], d, i['part_1'], i['part_2'], i['related_activity'], i['worth_remembering'] ]
      end
    }
  end
end

Axlsx::Package.new do |p|
  buildSheet(p, json_5ag, '5ag_notes')
  buildSheet(p, json_5at, '5at_notes')
  buildSheet(p, json_5bj, '5bj_notes')
  p.serialize('data/hg_fall_13/out/hg_fall_13_students_notes.xlsx')
end

puts '...done!'
