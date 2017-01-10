#!/usr/bin/env python

from __future__ import print_function
from collections import defaultdict as dd
import glob
import csv
import re
import os


r_title = re.compile(r"^(.+)_(.+)_(.+)$")
r_map = re.compile(r"\s+\[server\]\s+.+?\s+vs\.\s+.+?\s+on\s+(.+)")
r_winner = re.compile(r"\s+\[server\]\s+(.+?)\s+\([AB]\)\s+wins")

table = dd(lambda: dd(lambda: dd(lambda: 0)))
wins = dd(lambda: dd(lambda: 0))
losses = dd(lambda: dd(lambda: 0))

all_teams = set([])
all_maps = set([])


def report(the_map, winner, loser):
	table[the_map][winner][loser] += 1
	wins[the_map][winner] += 1
	losses[the_map][loser] += 1


for log in glob.glob("logs/*"):
	with open(log, "r") as f:
		print("processing", log, "...")
		content = f.read()
		title = r_title.search(log)
		team_a = title.group(2)
		team_b = title.group(3)
		maps = r_map.findall(content)
		winners = r_winner.findall(content)
		for i, winner in enumerate(winners):
			the_map = maps[i]
			if winner == team_a:
				report(the_map, team_a, team_b)
				report("summary", team_a, team_b)
			elif winner == team_b:
				report(the_map, team_b, team_a)
				report("summary", team_b, team_a)
		all_teams.add(team_a)
		all_teams.add(team_b)
		for the_map in maps:
			all_maps.add(the_map)

team_list = sorted(all_teams)
all_maps.add("summary")

for the_map in all_maps:
	path = os.path.join("reports", "%s.csv" % the_map)
	with open(path, "wb") as f:
		writer = csv.writer(f, delimiter=',', quotechar='"', quoting=csv.QUOTE_ALL)
		writer.writerow([""] + team_list + ["wins", "losses", "games"])  # header row
		for a in team_list:
			map_wins = wins[the_map][a]
			map_losses = losses[the_map][a]
			row = []
			for b in team_list:
				a_wins = table[the_map][a][b]
				b_wins = table[the_map][b][a]
				if a_wins + b_wins == 0:
					row.append("")
					continue
				row.append(round(1.*a_wins/(a_wins+b_wins), 4))
			writer.writerow([a] + row + [map_wins, map_losses, map_wins + map_losses])
	print("wrote report", path)
