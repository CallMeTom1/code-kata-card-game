package com.arena.cli;

import com.arena.stats.AggregateStats;
import com.arena.stats.MatchRecord;

import java.util.List;

/** What a batch produced: totals, and per-match records when they were asked for. */
public record RunReport(AggregateStats stats, List<MatchRecord> records) {
}
